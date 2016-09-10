package zstackui

public class MessageEvent {
	def String msgcorrid
	def String message
	
	public MessageEvent(String messageCorrId, String message){
		this.msgcorrid = messageCorrId;
		this.message = message;
	}
	public MessageEvent(){}
	
	public int getMsgcode() {
		return msgcorrid;
	}

	public String getMsg() {
		return message;
	}
}
