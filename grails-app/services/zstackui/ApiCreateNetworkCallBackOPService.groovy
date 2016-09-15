package zstackui


class ApiCreateNetworkCallBackOPService implements CallBackService{
    def message;
    private CallBackService service;
    private ApiCreateNetworkController apiCreateNetworkController;
    private UuidService uuidService
    private AsyncRabbitmqService asyncRabbitmqService
    private NetworkCreateMessage networkCreateMessage

    ApiCreateNetworkCallBackOPService(ApiCreateNetworkController apiCreateNetworkController,CallBackService service){
        this.apiCreateNetworkController = apiCreateNetworkController;
        this.service = service;
        //uuidService = new UuidService();
    };

    def sendMessage(String message,Boolean sendOrRollback){
        this.uuidService = new UuidService();
        asyncRabbitmqService = new AsyncRabbitmqService();
        //attach message information,send message
        asyncRabbitmqService.sendMessage(this,message,uuidService,sendOrRollback)
    }

    @Override
    def void success(){
        service.success()
    }

    @Override
    def void failed(){
        service.failed()
    }
}
