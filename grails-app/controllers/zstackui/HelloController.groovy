package zstackui

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate

class HelloController {

    def asyncRabbitmqService
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public HelloController(SimpMessagingTemplate messagingTemplate){
        this.messagingTemplate = messagingTemplate
    }

    def index() {
        println asyncRabbitmqService.getLastMessage()
        render(view: "index")
    }

    @MessageMapping("/hello")
    protected void hello(String world) {
        println "hello from controller, ${world}!"
        //return "hello from controller, ${world}!"
        this.messagingTemplate.convertAndSend("/topic/hello", "success")
    }
}
