package zstackui

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.RequestBody;

class ApiCreateNetworkController {

    private final SimpMessagingTemplate messagingTemplate;

    private ApiCreateNetworkService apiCreateNetworkService

    @Autowired
    public ApiCreateNetworkController(SimpMessagingTemplate messagingTemplate){
        this.messagingTemplate = messagingTemplate
    }

    @MessageMapping("/networkmessage")
    @SendTo("/topic/networkmessage")
    protected String send(NetworkCreateMessage networkCreateMessage) {
        //println "message is :"+networkCreateMessage.getName()
        //println "message password is : "+networkCreateMessage.getPassword()
        println "enter..."
        apiCreateNetworkService = new ApiCreateNetworkService(this,networkCreateMessage)
        apiCreateNetworkService.sendMessage()
        //println "enter..."
        return "success!"
    }

    def index() { }
}
