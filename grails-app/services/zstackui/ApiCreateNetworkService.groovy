package zstackui
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class ApiCreateNetworkService{

    def operations;
    private String networkMessage;
    private String rollbackMessage;
    private NetworkCreateMessage networkCreateMessage;
    private ApiCreateNetworkController apiCreateNetworkController;
    private static int i;
    private String
    private static Map object1;
    def object2;

    ApiCreateNetworkService(ApiCreateNetworkController apiCreateNetworkController,NetworkCreateMessage networkCreateMessage){
        this.apiCreateNetworkController = apiCreateNetworkController;
        this.networkCreateMessage = networkCreateMessage;
        i = 0;
        println "apinetworkservice started!"
        operations = [];
    }



    def sendMessage() {

        operations.add(new ApiCreateNetworkCallBackOPService(new CallBackService() {
            @Override
            void success(String replyMessage) {
                println "l2create success"
                def jsonSlurper = new JsonSlurper()
                def object = jsonSlurper.parseText(replyMessage)
                startNextOP(CreateL3VlanNetworkMsg((Map)object),"");
            }

            @Override
            void failed(String replyMessage) {
                println "l2create failed"
                def jsonSlurper = new JsonSlurper()
                def object = jsonSlurper.parseText(replyMessage)
                returnLastOP("","");
            }
        })
        )

        operations.add(new ApiCreateNetworkCallBackOPService(new CallBackService() {
            @Override
            void success(String replyMessage) {
                println "l3create success"
                def jsonSlurper = new JsonSlurper()
                def object = jsonSlurper.parseText(replyMessage)
                println startNextOP(AddDnsToL3NetworkMsg((Map)object),"")
            }

            @Override
            void failed(String replyMessage) {
                println "l3create failed"
                println returnLastOP("",CreateL2VlanNetworkRollbackMsg());
            }
        })
        )

        operations.add(new ApiCreateNetworkCallBackOPService(new CallBackService() {
            @Override
            void success(String replyMessage) {
                println "addDns success"
                def jsonSlurper = new JsonSlurper()
                def object = jsonSlurper.parseText(replyMessage)
                println startNextOP(AddIpRangeMsg((Map)object),"")
            }

            @Override
            void failed(String replyMessage) {
                println "addDns failed1"
                println returnLastOP(CreateL3VlanNetworkRollbackMsg());
            }
        })
        )

        operations.add(new ApiCreateNetworkCallBackOPService(new CallBackService() {
            @Override
            void success(String replyMessage) {
                println "addiprange success"
                def jsonSlurper = new JsonSlurper()
                def object = jsonSlurper.parseText(replyMessage)
                println startNextOP(QueryNetworkServiceProviderMsg((Map)object))
            }

            @Override
            void failed(String replyMessage) {
                println "addiprange failed1"
                println returnLastOP("");
            }
        })
        )

        operations.add(new ApiCreateNetworkCallBackOPService(new CallBackService() {
            @Override
            void success(String replyMessage) {
                println "queryNetworkServiceProviderMsg success"
                def jsonSlurper = new JsonSlurper()
                def object = jsonSlurper.parseText(replyMessage)
                println startNextOP(AttachNetworkServiceToL3NetworkMsg((Map)object))
            }

            @Override
            void failed(String replyMessage) {
                println "queryNetworkServiceProviderMsg failed1"
                println returnLastOP("");
            }
        })
        )

        operations.add(new ApiCreateNetworkCallBackOPService(new CallBackService() {
            @Override
            void success(String replyMessage) {
                println "attachNetworkServiceToL3NetworkMsg success"
                def jsonSlurper = new JsonSlurper()
                def object = jsonSlurper.parseText(replyMessage)
                reportAllOperationsComplete(replyMessage);
            }

            @Override
            void failed(String replyMessage) {
                println "attachNetworkServiceToL3NetworkMsg failed1"
                println returnLastOP("");
            }
        })
        )

        //in the beginning
        networkMessage = CreateL2VlanNetworkMsg();
        rollbackMessage = "";
        println startNextOP(CreateL2VlanNetworkMsg());
    }



    def String CreateL2VlanNetworkMsg(){
        println "L2 is called"
        def apil2vlanMsg = [:]
        def createl2vlannetworkmsg = [:];
        def sessionMsg = [:]

        sessionMsg.put("uuid",networkCreateMessage.getSessionUuid())
        sessionMsg.put("callid",networkCreateMessage.getCallid())

        createl2vlannetworkmsg.put("name",networkCreateMessage.getName()+"-L2");
        createl2vlannetworkmsg.put("type",networkCreateMessage.getL2type());
        createl2vlannetworkmsg.put("zoneUuid",networkCreateMessage.getZoneUuid())
        createl2vlannetworkmsg.put("physicalInterface",networkCreateMessage.getPhysicalInterface())
        createl2vlannetworkmsg.put("session",sessionMsg);

        apil2vlanMsg.put('org.zstack.header.network.l2.APICreateL2NoVlanNetworkMsg',createl2vlannetworkmsg);

        def L2String = new JsonBuilder(apil2vlanMsg).toString()
        println "L2String is :"+L2String;
        return L2String;

    }


    def String CreateL2VlanNetworkRollbackMsg(){
        println "L2 rollback is called"
        def apil2vlanrollbackMsg = [:]
        def createl2vlannetworkrollbackmsg = [:];
        def sessionMsg = [:]


        sessionMsg.put("uuid",networkCreateMessage.getSessionUuid())
        sessionMsg.put("callid",networkCreateMessage.getCallid())

        createl2vlannetworkrollbackmsg.put("uuid",object1.inventory.uuid);
        createl2vlannetworkrollbackmsg.put("session",sessionMsg);

        apil2vlanrollbackMsg.put('org.zstack.header.network.l2.APIDeleteL2NetworkMsg',createl2vlannetworkrollbackmsg);

        def L2RollbackString = new JsonBuilder(apil2vlanrollbackMsg).toString()
        println "L2RollbackString is :"+L2RollbackString;
        return L2RollbackString;
    }

    def String CreateL3VlanNetworkMsg(Map object){
        this.object1 = object.values()[0];
        println "L3 is called"

        def apiL3vlanMsg = [:]
        def createl3vlannetworkmsg = [:];
        def sessionMsg = [:]

        sessionMsg.put("uuid",networkCreateMessage.getSessionUuid())
        sessionMsg.put("callid",networkCreateMessage.getCallid())

        createl3vlannetworkmsg.put("name",networkCreateMessage.getName());
        createl3vlannetworkmsg.put("type",networkCreateMessage.getL3type());
        createl3vlannetworkmsg.put("l2NetworkUuid",this.object1.inventory.uuid);

        createl3vlannetworkmsg.put("physicalInterface",networkCreateMessage.getPhysicalInterface())
        createl3vlannetworkmsg.put("session",sessionMsg);

        apiL3vlanMsg.put('org.zstack.header.network.l3.APICreateL3NetworkMsg',createl3vlannetworkmsg);

        def L3String = new JsonBuilder(apiL3vlanMsg).toString()
        println "L2String is :"+L3String;

        return L3String;
    }

    def String CreateL3VlanNetworkRollbackMsg(Map object){
        println "L3 rollback is called"
        def apil3vlanrollbackMsg = [:]
        def createl3vlannetworkrollbackmsg = [:];
        def sessionMsg = [:]

        sessionMsg.put("uuid",networkCreateMessage.getSessionUuid())
        sessionMsg.put("callid",networkCreateMessage.getCallid())

        createl3vlannetworkrollbackmsg.put("uuid",object1.inventory.uuid);
        createl3vlannetworkrollbackmsg.put("session",sessionMsg);

        apil3vlanrollbackMsg.put('org.zstack.header.network.l2.APIDeleteL2NetworkMsg',createl3vlannetworkrollbackmsg);

        def L3RollbackString = new JsonBuilder(apil3vlanrollbackMsg).toString()
        println "L3RollbackString is :"+L3RollbackString;
        return L3RollbackString;
    }

    def String AddDnsToL3NetworkMsg(Map object){
        this.object1 = object.values()[0];
        println "AddDns is called"
        def apiAddDns = [:]
        def addDnsToL3NetworkMsg = [:];
        def sessionMsg = [:]

        sessionMsg.put("uuid",networkCreateMessage.getSessionUuid())
        sessionMsg.put("callid",networkCreateMessage.getCallid())

        addDnsToL3NetworkMsg.put("dns",networkCreateMessage.getDns());
        addDnsToL3NetworkMsg.put("l3NetworkUuid",object1.inventory.uuid);

        addDnsToL3NetworkMsg.put("session",sessionMsg);

        apiAddDns.put('org.zstack.header.network.l3.APIAddDnsToL3NetworkMsg',addDnsToL3NetworkMsg);

        def addDnsString = new JsonBuilder(apiAddDns).toString()
        println "addDns string is :"+addDnsString;

        return addDnsString;
    }

    def String AddIpRangeMsg(Map object){
        this.object1 = object.values()[0];
        println "AddIpRange is called"
        def apiAddIpRange = [:]
        def addIpRange = [:];
        def sessionMsg = [:]

        sessionMsg.put("uuid",networkCreateMessage.getSessionUuid())
        sessionMsg.put("callid",networkCreateMessage.getCallid())

        addIpRange.put("l3NetworkUuid",object1.inventory.uuid)
        addIpRange.put("name",networkCreateMessage.getStartIp())
        addIpRange.put("startIp",networkCreateMessage.getStartIp())
        addIpRange.put("endIp",networkCreateMessage.getEndIp())
        addIpRange.put("gateway",networkCreateMessage.getGateway());
        addIpRange.put("netmask",networkCreateMessage.getNetmask());

        addIpRange.put("session",sessionMsg);
        apiAddIpRange.put('org.zstack.header.network.l3.APIAddIpRangeMsg',addIpRange);

        def addIpRangeString = new JsonBuilder(apiAddIpRange).toString()
        println "addDns string is :"+addIpRangeString;

        return addIpRangeString;
    }

    def String QueryNetworkServiceProviderMsg(Map object){
        object1 = object.values()[0];
        println "QueryNetworkServiceProviderMsg is called"
        def apiQueryNetworkServiceProviderMsg = [:]
        def QueryNetworkServiceProviderMsg = [:];
        def sessionMsg = [:]

        sessionMsg.put("uuid",networkCreateMessage.getSessionUuid())
        sessionMsg.put("callid",networkCreateMessage.getCallid())

        QueryNetworkServiceProviderMsg.put("count",false);
        QueryNetworkServiceProviderMsg.put("start",0);
        QueryNetworkServiceProviderMsg.put("replyWithCount",true);
        QueryNetworkServiceProviderMsg.put("conditions",[]);

        QueryNetworkServiceProviderMsg.put("session",sessionMsg);

        apiQueryNetworkServiceProviderMsg.put('org.zstack.header.network.service.APIQueryNetworkServiceProviderMsg',QueryNetworkServiceProviderMsg);

        def queryNetworkServiceProviderMsg = new JsonBuilder(apiQueryNetworkServiceProviderMsg).toString()
        println "queryNetworkServiceProviderMsg string is :"+queryNetworkServiceProviderMsg;

        return queryNetworkServiceProviderMsg;
    }

    def String AttachNetworkServiceToL3NetworkMsg(Map object){
        this.object2 = object.values()[0];
        println "AttachNetworkServiceToL3NetworkMsg object1 is :"+object1;
        println "AttachNetworkServiceToL3NetworkMsg is called"
        def apiAttachNetworkServiceToL3NetworkMsg = [:]
        def attachNetworkServiceToL3NetworkMsg = [:];
        def sessionMsg = [:]

        sessionMsg.put("uuid",networkCreateMessage.getSessionUuid())
        sessionMsg.put("callid",networkCreateMessage.getCallid())

        attachNetworkServiceToL3NetworkMsg.put("l3NetworkUuid",object1.inventory.l3NetworkUuid);
        def networkService = [:]
        networkService.put(this.object2.inventories[0].uuid,[
                                                                "DHCP",
                                                                "Userdata",
                                                                "Eip"
                                                        ])

        attachNetworkServiceToL3NetworkMsg.put("networkServices",networkService);
        attachNetworkServiceToL3NetworkMsg.put("session",sessionMsg);

        apiAttachNetworkServiceToL3NetworkMsg.put('org.zstack.header.network.service.APIAttachNetworkServiceToL3NetworkMsg',attachNetworkServiceToL3NetworkMsg);

        def attachNetworkServiceToL3NetworkMsg1 = new JsonBuilder(apiAttachNetworkServiceToL3NetworkMsg).toString()
        println "attachNetworkServiceToL3NetworkMsg string is :"+attachNetworkServiceToL3NetworkMsg1;

        return attachNetworkServiceToL3NetworkMsg1;
    }


    def String startNextOP(String sendMessage){
        println "[important!]i is :"+i
        if (i > this.operations.size()-1){
            return "i is out of index!";
        }
        ApiCreateNetworkCallBackOPService op = this.operations.get(i);
        op.sendMessage(sendMessage,true);
        if (i < this.operations.size()-1){
            i++;
            return "i++";
        }
        return "the last step!"
    }

    def String returnLastOP(String rollbackMessage){
        println "operations"+i+" is called!"
        if (i >= this.operations.size()){
            return "all operations finished,dont need rollback!"
        }
        if (i < 0){
            return  "rollback finished!"
        }
        ApiCreateNetworkCallBackOPService op = this.operations.get(i);
        op.sendMessage(rollbackMessage,false);
        i--;
    }

    def String reportAllOperationsComplete(String message){
        i =0;
        apiCreateNetworkController.messageListener(message);
        return "all step success!";
    }
}
