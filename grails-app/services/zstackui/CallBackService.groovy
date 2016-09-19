package zstackui


interface CallBackService {

    //def void MessageHandler(String message);
    def void success(String replyMessage)
    def void failed(String replyMessage)
}
