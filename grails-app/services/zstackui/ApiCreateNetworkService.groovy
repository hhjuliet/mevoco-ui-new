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
        //in the beginning
        networkMessage = CreateL2VlanNetworkMsg();
        i = 0;
        println "apinetworkservice started!"
        operations = [];
    }



    def sendMessage() {

        operations.add(new ApiCreateNetworkCallBackOPService(apiCreateNetworkController,new CallBackService() {
            @Override
            void success() {
                println "callback success0"
                networkMessage = CreateL3VlanNetworkMsg();
                startNextOP()
            }

            @Override
            void failed() {
                println "callback failed0"
                rollbackMessage = CreateL3VlanNetworkMsg();
                returnLastOP();
            }
        })
        )

        operations.add(new ApiCreateNetworkCallBackOPService(apiCreateNetworkController,new CallBackService() {
            @Override
            void success() {
                println "callback success1"
                networkMessage = CreateL3VlanNetworkMsg();
                println startNextOP()
            }

            @Override
            void failed() {
                println "callback failed1"
                rollbackMessage = CreateL3VlanNetworkMsg();
                println returnLastOP();
            }
        })
        )

        println startNextOP();
        //println "apinetworkservice send message!"
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
    }

    def String startNextOP(){
        println "【important!】i is :"+i
        ApiCreateNetworkCallBackOPService op = this.operations.get(i);
        op.sendMessage(networkMessage,true);

        if (i == this.operations.size()-1){
            return reportAllOperationsComplete();
        }else{
            i++;
            return "i++";
        }

    }

    def String returnLastOP(){
        println "【important!】i is :"+i
        if (i >= this.operations.size()){
            return "all operations finished,dont need rollback!"
        }
        if (i<0){
            return  "rollback finished!"
        }
        ApiCreateNetworkCallBackOPService op = this.operations.get(i);
        op.sendMessage(rollbackMessage,false);
        i--;
    }

    def String reportAllOperationsComplete(){
        return "success!"
        i =0;
    }
}
