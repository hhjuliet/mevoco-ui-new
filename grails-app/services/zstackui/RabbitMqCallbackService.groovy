package zstackui



interface RabbitmqCallbackService {

    //def void MessageHandler(String message);
    def void success(String replymessage)
    def void failed(String replymessage)
}
