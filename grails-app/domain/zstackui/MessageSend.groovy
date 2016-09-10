package zstackui

public class MessageSend {
	def String msgcorrid
	def String message
	
	public MessageEvent(String messageCorrId, String message){
		this.msgcorrid = messageCorrId;
		this.message = message;
	}
	public int getMsgcode() {
		return msgcorrid;
	}

	public String getMsg() {
		return msgcorrid;
	}
}
