package zstackui

class ApiCreateNetworkCallBackOPService implements CallBackService{
    def message;
    private CallBackService service;
    private UuidService uuidService
    private AsyncRabbitmqService asyncRabbitmqService

    ApiCreateNetworkCallBackOPService(CallBackService service){
        this.service = service;
    };

    def sendMessage(String sendMessage,Boolean sendOrRollback){
        this.uuidService = new UuidService();
        asyncRabbitmqService = new AsyncRabbitmqService();
        //attach message information,send message
        asyncRabbitmqService.sendMessage(this,sendMessage,uuidService,sendOrRollback)
    }

    @Override
    def void success(String replyMessage){
        service.success(replyMessage)
    }

    @Override
    def void failed(String replyMessage){
        service.failed(replyMessage)
    }
}
