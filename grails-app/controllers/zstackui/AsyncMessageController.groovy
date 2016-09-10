package zstackui
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import static grails.async.Promises.*

class AsyncMessageController{
	
	def asyncRabbitmqService;
	def messageEvent;
	def message = '''
{
  "org.zstack.header.identity.APILogInByAccountMsg": {
    "accountName": "admin",
    "password": "b109f3bbbc244eb82441917ed06d618b9008dd09b3befd1b5e07394c706a8bb980b1d7785e5976ec049b46df5f1326af5a2ea6d103fd07c95385ffab0cacbc86"
  }
}'''
	def index(){
		
	}

	@MessageMapping("/sendMessage")
    public void sendMessage(String sendMessage) {
		def reply;
		asyncRabbitmqService = new AsyncRabbitmqService();
		if (sendMessage) {
			asyncRabbitmqService.sendMessage(sendMessage)
		}else{
			reply = 'failed'
		}
	}
	
	
	@Subscribe
	def void messageListener(MessageEvent event){
		println "===================================================================="
		println "event1 is: "+event.getMsg();
		//println "message is : "+messageReturn(event.getMsg())
	}
	
	def String messageReturn(String message){
		return message
	}
}
