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
	AsyncRabbitmqService asyncRabbitmqService;
	def parser;

    ApiCreateNetworkService(ApiCreateNetworkController apiCreateNetworkController,NetworkCreateMessage networkCreateMessage){
        this.apiCreateNetworkController = apiCreateNetworkController;
        this.networkCreateMessage = networkCreateMessage;
        i = 0;
        println "apinetworkservice started!"
        operations = [];
		asyncRabbitmqService = new AsyncRabbitmqService();
		parser = new JsonSlurper();

    }



    def sendMessage() {
        new ScopeService() {
            String l2Uuid;
            String l3Uuid;
			String networkUuid;

            void setup() {
                addStep(new Runner() {
                    @Override
                    void run(Callback cb) {

                        UuidService uuidService = new UuidService();
                        asyncRabbitmqService.sendMessage(CreateL2VlanNetworkMsg(),uuidService,true,new RabbitmqCallbackService(){
                            @Override
                            void success(String replyMessage) {
								def obj = parser.parseText(replyMessage).values()[0];
								if (obj["success"] == true){
									l2Uuid = obj["inventory"]["uuid"];
									cb.success();
								}else{
									cb.failed(2)
								}

                            }

                            @Override
                            void failed(String replymesssge) {
                                cb.failed(1);
                            }
                        })

                    }
                })

                addStep(new Runner() {
                    @Override
                    void run(Callback cb) {
                        // create l3 network
						UuidService uuidService = new UuidService();
						asyncRabbitmqService.sendMessage(CreateL3VlanNetworkMsg(l2Uuid),uuidService,true,new RabbitmqCallbackService(){
							@Override
							void success(String replyMessage) {
								def obj = parser.parseText(replyMessage).values()[0];
								if (obj["success"] == true){
									l3Uuid = obj["inventory"]["uuid"];
									cb.success();
								}else{
									cb.failed(2)
								}
							}

							@Override
							void failed(String replymesssge) {
								cb.failed(1);
							}
						})
                    }
                })

				addStep(new Runner() {
					@Override
					void run(Callback cb) {
						// create l3 network
						UuidService uuidService = new UuidService();
						asyncRabbitmqService.sendMessage(AddDnsToL3NetworkMsg(l3Uuid),uuidService,true,new RabbitmqCallbackService(){
							@Override
							void success(String replyMessage) {
								def obj = parser.parseText(replyMessage).values()[0];
								if (obj["success"] == true){
									cb.success();
								}else{
									cb.failed(2)
								}
							}

							@Override
							void failed(String replymesssge) {
								cb.failed(1);
							}
						})
					}
				})

				addStep(new Runner() {
					@Override
					void run(Callback cb) {
						// create l3 network
						UuidService uuidService = new UuidService();
						asyncRabbitmqService.sendMessage(AddIpRangeMsg(l3Uuid),uuidService,true,new RabbitmqCallbackService(){
							@Override
							void success(String replyMessage) {
								def obj = parser.parseText(replyMessage).values()[0];
								if (obj["success"] == true){
									cb.success();
								}else{
									cb.failed(2)
								}
							}

							@Override
							void failed(String replymesssge) {
								cb.failed(1);
							}
						})
					}
				})

				addStep(new Runner() {
					@Override
					void run(Callback cb) {
						// create l3 network
						UuidService uuidService = new UuidService();
						asyncRabbitmqService.sendMessage(QueryNetworkServiceProviderMsg(),uuidService,true,new RabbitmqCallbackService(){
							@Override
							void success(String replyMessage) {

								def obj = parser.parseText(replyMessage).values()[0];
								if (obj["success"] == true){
									networkUuid = obj["inventories"][1]["uuid"];
									cb.success();
								}else{
									cb.failed(2)
								}

							}

							@Override
							void failed(String replymesssge) {
								cb.failed(1);
							}
						})
					}
				})

				addStep(new Runner() {
					@Override
					void run(Callback cb) {
						// create l3 network
						UuidService uuidService = new UuidService();
						asyncRabbitmqService.sendMessage(AttachNetworkServiceToL3NetworkMsg(l3Uuid,networkUuid),uuidService,true,new RabbitmqCallbackService(){
							@Override
							void success(String replyMessage) {
								def obj = parser.parseText(replyMessage).values()[0];
								if (obj["success"] == true){
									reportAllOperationsComplete(replyMessage)
								}else{
									cb.failed(2)
								}
							}

							@Override
							void failed(String replymesssge) {
								cb.failed(1);
							}
						})
					}
				})
            }

            void done() {
                println "hello world"
            }

            void error(int i) {
                println "error " + i
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



    def String reportAllOperationsComplete(String message){
        i =0;
        apiCreateNetworkController.messageListener(message);
        return "all step success!";
    }
}
