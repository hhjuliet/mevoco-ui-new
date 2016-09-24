package zstackui
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class ApiCreateNetworkNewService{

    def operations;
    private String networkMessage;
    private String rollbackMessage;
    private NetworkCreateMessage networkCreateMessage;
    private ApiCreateNetworkController apiCreateNetworkController;
    private static int i;
    private String
    private static Map object1;
    def object2;
    AsyncRabbitmqService asyncRabbitmqService;
    def parser;

    ApiCreateNetworkNewService(ApiCreateNetworkController apiCreateNetworkController,NetworkCreateMessage networkCreateMessage){
        this.apiCreateNetworkController = apiCreateNetworkController;
        this.networkCreateMessage = networkCreateMessage;
        i = 0;
        println "apinetworkservice started!"
        operations = [];
        asyncRabbitmqService = new AsyncRabbitmqService();
        parser = new JsonSlurper();

    }



    def sendMessage() {
        new SimpleGotoFlowChain() {
            String l2Uuid;
            String l3Uuid;
            String networkUuid;

            @Override
            def void done() {
                println "finished!";
            }

            @Override
            def void setup() {
                addFlow("step1",new Flow() {
                    @Override
                    public void run(FlowTrigger trigger){
                        //create l2 network
                        asyncRabbitmqService.sendMessage(CreateL2VlanNetworkMsg(),new UuidService(),true,new RabbitmqCallbackService(){
                            @Override
                            void success(String replyMessage) {
                                def obj = parser.parseText(replyMessage).values()[0];
                                if (obj["success"] == true){
                                    l2Uuid = obj["inventory"]["uuid"];
                                    trigger.next("step2");
                                }else{
                                    sendMessageToController(replyMessage);
                                    trigger.failed("create l2network background failed, reply message is: "+replyMessage);
                                }

                            }

                            @Override
                            void failed(String replymesssge) {
                                println "create l2network rabbitmq failed, reply message is"+replymesssge;
                            }
                        })
                    }

                    @Override
                    public void rollback(FlowRollback trigger) {
                        System.out.println("i am rollbacking createl2network...");
                        trigger.rollback();
                    }
                });

                addFlow("step2",new Flow() {
                    @Override
                    public void run(FlowTrigger trigger){
                        // create l3 network
                        asyncRabbitmqService.sendMessage(CreateL3VlanNetworkMsg(l2Uuid),new UuidService(),true,new RabbitmqCallbackService(){
                            @Override
                            void success(String replyMessage) {
                                def obj = parser.parseText(replyMessage).values()[0];
                                if (obj["success"] == true){
                                    l3Uuid = obj["inventory"]["uuid"];
                                    trigger.next("step3");
                                }else{
                                    sendMessageToController(replyMessage);
                                    trigger.failed("create l3network background failed, reply message is: "+replyMessage);
                                }
                            }

                            @Override
                            void failed(String replymesssge) {
                                println "create l3network rabbitmq failed, reply message is"+replymesssge;
                            }
                        })
                    }

                    @Override
                    public void rollback(FlowRollback trigger) {
                        System.out.println("i am rollbacking createl3network...");
                        trigger.rollback();
                    }
                });

                addFlow("step3",new Flow() {
                    @Override
                    public void run(FlowTrigger trigger){
                        //addDnstol3
                        asyncRabbitmqService.sendMessage(AddDnsToL3NetworkMsg(l3Uuid),new UuidService(),true,new RabbitmqCallbackService(){
                            @Override
                            void success(String replyMessage) {
                                def obj = parser.parseText(replyMessage).values()[0];
                                if (obj["success"] == true){
                                    trigger.next("step4");
                                }else{
                                    sendMessageToController(replyMessage);
                                    trigger.failed("adddns to l3network background failed, reply message is: "+replyMessage);
                                }
                            }

                            @Override
                            void failed(String replymesssge) {
                                println "adddns to l3network rabbitmq failed, reply message is"+replymesssge;
                            }
                        })
                    }

                    @Override
                    public void rollback(FlowRollback trigger) {
                        System.out.println("i am rollbacking adddns...");
                        trigger.rollback();
                    }
                });

                addFlow("step4",new Flow() {
                    @Override
                    public void run(FlowTrigger trigger){
                        // add ip range
                        asyncRabbitmqService.sendMessage(AddIpRangeMsg(l3Uuid),new UuidService(),true,new RabbitmqCallbackService(){
                            @Override
                            void success(String replyMessage) {
                                def obj = parser.parseText(replyMessage).values()[0];
                                if (obj["success"] == true){
                                    trigger.next("step5");
                                }else{
                                    sendMessageToController(replyMessage);
                                    trigger.failed("add ip range background failed, reply message is: "+replyMessage)
                                }
                            }

                            @Override
                            void failed(String replymesssge) {
                                println "add ip range rabbitmq failed, reply message is"+replymesssge;
                            }
                        })
                    }

                    @Override
                    public void rollback(FlowRollback trigger) {
                        System.out.println("i am rollbacking add ip range...");
                        trigger.rollback();
                    }
                });

                addFlow("step5",new Flow() {
                    @Override
                    public void run(FlowTrigger trigger){
                        //Query NetworkService ProviderMsg
                        UuidService uuidService = new UuidService();
                        asyncRabbitmqService.sendMessage(QueryNetworkServiceProviderMsg(),uuidService,true,new RabbitmqCallbackService(){
                            @Override
                            void success(String replyMessage) {

                                def obj = parser.parseText(replyMessage).values()[0];
                                if (obj["success"] == true){
                                    networkUuid = obj["inventories"][1]["uuid"];
                                    trigger.next("step6");
                                }else{
                                    sendMessageToController(replyMessage);
                                    trigger.failed("Query NetworkService background failed, reply message is:"+replyMessage);
                                }

                            }

                            @Override
                            void failed(String replymesssge) {
                                println "Query NetworkService rabbitmq failed, reply message is : "+replymesssge
                            }
                        })
                    }

                    @Override
                    public void rollback(FlowRollback trigger) {
                        System.out.println("i am rollbacking Query NetworkService...");
                        trigger.rollback();
                    }
                });

                addFlow("step6",new Flow() {
                    @Override
                    public void run(FlowTrigger trigger){
                        // Attach NetworkService To L3NetworkMsg
                        UuidService uuidService = new UuidService();
                        asyncRabbitmqService.sendMessage(AttachNetworkServiceToL3NetworkMsg(l3Uuid,networkUuid),uuidService,true,new RabbitmqCallbackService(){
                            @Override
                            void success(String replyMessage) {
                                def obj = parser.parseText(replyMessage).values()[0];
                                if (obj["success"] == true){
                                    sendMessageToController(replyMessage);
                                }else{
                                    sendMessageToController(replyMessage);
                                    trigger.failed("add ip range background failed, reply message is: "+replyMessage);
                                }
                            }

                            @Override
                            void failed(String replymesssge) {
                                println "attach NetworkService To L3NetworkMsg rabbitmq failed, reply message is : "+replymesssge
                            }
                        })
                    }

                    @Override
                    public void rollback(FlowRollback trigger) {
                        System.out.println("i am rollbacking attach NetworkService To L3NetworkMsg");
                        trigger.rollback();
                    }
                });
            }
        }.start();
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

    def String CreateL3VlanNetworkMsg(String l2uuid){
        println "L3 is called"

        def apiL3vlanMsg = [:]
        def createl3vlannetworkmsg = [:];
        def sessionMsg = [:]

        sessionMsg.put("uuid",networkCreateMessage.getSessionUuid())
        sessionMsg.put("callid",networkCreateMessage.getCallid())

        createl3vlannetworkmsg.put("name",networkCreateMessage.getName());
        createl3vlannetworkmsg.put("type",networkCreateMessage.getL3type());
        createl3vlannetworkmsg.put("l2NetworkUuid",l2uuid);

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

    def String AddDnsToL3NetworkMsg(String l3Uuid){
        println "AddDns is called"
        def apiAddDns = [:]
        def addDnsToL3NetworkMsg = [:];
        def sessionMsg = [:]

        sessionMsg.put("uuid",networkCreateMessage.getSessionUuid())
        sessionMsg.put("callid",networkCreateMessage.getCallid())

        addDnsToL3NetworkMsg.put("dns",networkCreateMessage.getDns());
        addDnsToL3NetworkMsg.put("l3NetworkUuid",l3Uuid);

        addDnsToL3NetworkMsg.put("session",sessionMsg);

        apiAddDns.put('org.zstack.header.network.l3.APIAddDnsToL3NetworkMsg',addDnsToL3NetworkMsg);

        def addDnsString = new JsonBuilder(apiAddDns).toString()
        println "addDns string is :"+addDnsString;

        return addDnsString;
    }

    def String AddIpRangeMsg(String l3Uuid){
        println "AddIpRange is called"
        def apiAddIpRange = [:]
        def addIpRange = [:];
        def sessionMsg = [:]

        sessionMsg.put("uuid",networkCreateMessage.getSessionUuid())
        sessionMsg.put("callid",networkCreateMessage.getCallid())

        addIpRange.put("l3NetworkUuid",l3Uuid)
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

    def String QueryNetworkServiceProviderMsg(){
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

    def String AttachNetworkServiceToL3NetworkMsg(String l3Uuid,String networkUuid){
        println "AttachNetworkServiceToL3NetworkMsg is called"
        def apiAttachNetworkServiceToL3NetworkMsg = [:]
        def attachNetworkServiceToL3NetworkMsg = [:];
        def sessionMsg = [:]

        sessionMsg.put("uuid",networkCreateMessage.getSessionUuid())
        sessionMsg.put("callid",networkCreateMessage.getCallid())

        attachNetworkServiceToL3NetworkMsg.put("l3NetworkUuid",l3Uuid);
        def networkService = [:]
        networkService.put(networkUuid,[
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



    def String sendMessageToController(String message){
        i =0;
        apiCreateNetworkController.messageListener(message);
        return "all step success!";
    }
}
