package zstackui


class MessageEvent {
    def  msgcorrid;
    def  message;

    public MessageEvent(String messageCorrId, String message){
        this.msgcorrid = messageCorrId;
        this.message = message;
    }
    public MessageEvent(){}
}
