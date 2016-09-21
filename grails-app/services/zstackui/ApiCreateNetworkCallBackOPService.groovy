package zstackui

class ApiCreateNetworkCallBackOPService{
    def message;
    //private CallBackService service;
    private UuidService uuidService
    private AsyncRabbitmqService asyncRabbitmqService

    /*ApiCreateNetworkCallBackOPService(CallBackService service){
        this.service = service;
    };*/

    def sendMessage(String sendMessage,Boolean sendOrRollback){
        this.uuidService = new UuidService();
        asyncRabbitmqService = new AsyncRabbitmqService();
        //attach message information,send message
        asyncRabbitmqService.sendMessage(this,sendMessage,uuidService,sendOrRollback)
    }

    def void success(){
        service.success()
    }

    def void failed(){
        service.failed()
    }
}
