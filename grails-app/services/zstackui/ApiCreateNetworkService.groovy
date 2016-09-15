package zstackui

class ApiCreateNetworkService{

    def operations;
    private String networkMessage;
    private String rollbackMessage;
    private NetworkCreateMessage networkCreateMessage;
    private ApiCreateNetworkController apiCreateNetworkController;
    private static int i;

    ApiCreateNetworkService(ApiCreateNetworkController apiCreateNetworkController,NetworkCreateMessage networkCreateMessage){
        this.apiCreateNetworkController = apiCreateNetworkController;
        this.networkCreateMessage = networkCreateMessage;
        i = 0;
        println "apinetworkservice started!"
        operations = [];
    }



    def sendMessage() {

        networkMessage = CreateL2VlanNetworkMsg();
        rollbackMessage = CreateL2VlanNetworkMsg();
        operations.add(new ApiCreateNetworkCallBackOPService(apiCreateNetworkController,new CallBackService() {
            @Override
            void success() {
                println "callback success0"
                startNextOP()
            }

            @Override
            void failed() {
                println "callback failed0"
                returnLastOP();
            }
        },networkMessage,rollbackMessage)
        )

        networkMessage = CreateL3VlanNetworkMsg();
        rollbackMessage = CreateL3VlanNetworkMsg();
        operations.add(new ApiCreateNetworkCallBackOPService(apiCreateNetworkController,new CallBackService() {
            @Override
            void success() {
                println "callback success1"
                println startNextOP()
            }

            @Override
            void failed() {
                println "callback failed1"
                println returnLastOP();
            }
        },networkMessage,rollbackMessage)
        )

        //in the beginning
        networkMessage = CreateL2VlanNetworkMsg();
        rollbackMessage = CreateL2VlanNetworkMsg();
        println startNextOP();
    }



    def String CreateL2VlanNetworkMsg(){
        /*def createl2vlannetworkmsg = [:];
        createl2vlannetworkmsg.put("name":networkCreateMessage.getName()+"-L2")
        createl2vlannetworkmsg.put("type":networkCreateMessage.getL2type())
        createl2vlannetworkmsg.put("zoneUuid":networkCreateMessage.getZoneUuid())
        createl2vlannetworkmsg.put("physicalInterface":networkCreateMessage.getPhysicalInterface())
        createl2vlannetworkmsg.put("vlan":networkCreateMessage.getVlan())*/
        return '''{
                      "org.zstack.header.identity.APILogInByAccountMsg": {
                       "accountName": "admin",
                       "password": "b109f3bbbc244eb82441917ed06d618b9008dd09b3befd1b5e07394c706a8bb980b1d7785e5976ec049b46df5f1326af5a2ea6d103fd07c95385ffab0cacbc86"
                      }
                 }'''
        println "L2 is called"
    }

    def String CreateL3VlanNetworkMsg(){
        /*def createl2vlannetworkmsg = [:];
        createl2vlannetworkmsg.put("name":networkCreateMessage.getName()+"-L2")
        createl2vlannetworkmsg.put("type":networkCreateMessage.getL2type())
        createl2vlannetworkmsg.put("zoneUuid":networkCreateMessage.getZoneUuid())
        createl2vlannetworkmsg.put("physicalInterface":networkCreateMessage.getPhysicalInterface())
        createl2vlannetworkmsg.put("vlan":networkCreateMessage.getVlan())*/
        return '''{
                      "org.zstack.header.identity.APILogInByAccountMsg": {
                       "accountName": "admin",
                       "password": "b109f3bbbc244eb82441917ed06d618b9008dd09b3befd1b5e07394c706a8bb980b1d7785e5976ec049b46df5f1326af5a2ea6d103fd07c95385ffab0cacbc86"
                      }
                 }'''
        println "L3 is called"
    }

    def String startNextOP(){
        println "[important!]i is :"+i
        if (i >= this.operations.size()){
            return reportAllOperationsComplete();
        }
        ApiCreateNetworkCallBackOPService op = this.operations.get(i);
        op.sendMessage(true);
        if (i < this.operations.size()-1){
            i++;
            return "i++";
        }
        return "the last step!"

    }

    def String returnLastOP(){
        println "operations"+i+" is called!"
        if (i >= this.operations.size()){
            return "all operations finished,dont need rollback!"
        }
        if (i < 0){
            return  "rollback finished!"
        }
        ApiCreateNetworkCallBackOPService op = this.operations.get(i);
        op.sendMessage(false);
        i--;
    }

    def String reportAllOperationsComplete(){
        return "success!"
        i =0;
    }
}
