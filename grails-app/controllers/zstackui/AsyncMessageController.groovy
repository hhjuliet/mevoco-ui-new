package zstackui
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.messaging.simp.SimpMessagingTemplate

import static grails.async.Promises.*

class AsyncMessageController{

	def asyncRabbitmqService;
	private final SimpMessagingTemplate messagingTemplate;

	@Autowired
	public AsyncMessageController(SimpMessagingTemplate messagingTemplate){
		this.messagingTemplate = messagingTemplate
	}

	@MessageMapping("/message")
    protected void send(String sendMessage) {
		def reply;
		asyncRabbitmqService = new AsyncRabbitmqService(this);
		if (sendMessage) {
			asyncRabbitmqService.sendMessage(sendMessage)
		}else{
			reply = 'failed'
		}
	}


	def void messageListener(MessageEvent event){
		println "===================================================================="
		println "event3 is: "+event.getMessage();
		this.messagingTemplate.convertAndSend("/topic/message", event.getMessage())
	}


}
