package zstackui


class ApiCreateNetworkCallBackOPService implements CallBackService{
    def message;
    private CallBackService service;
    private ApiCreateNetworkController apiCreateNetworkController;
    private UuidService uuidService
    private AsyncRabbitmqService asyncRabbitmqService
    private NetworkCreateMessage networkCreateMessage
    private String networkMessage;
    private String rollbackMessage;

    ApiCreateNetworkCallBackOPService(ApiCreateNetworkController apiCreateNetworkController,CallBackService service,String networkMessage,String rollbackMessage){
        this.apiCreateNetworkController = apiCreateNetworkController;
        this.service = service;
        this.rollbackMessage = rollbackMessage;
        this.networkMessage = networkMessage;
        //uuidService = new UuidService();
    };

    def sendMessage(Boolean sendOrRollback){
        this.uuidService = new UuidService();
        asyncRabbitmqService = new AsyncRabbitmqService();
        //attach message information,send message
        if (sendOrRollback){
            asyncRabbitmqService.sendMessage(this,this.networkMessage,uuidService,sendOrRollback)
        }else {
            asyncRabbitmqService.sendMessage(this,this.rollbackMessage,uuidService,sendOrRollback)
        }

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
