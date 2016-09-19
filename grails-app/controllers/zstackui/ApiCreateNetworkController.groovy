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
    protected String send(NetworkCreateMessage networkCreateMessage) {
        println "enter..."
        apiCreateNetworkService = new ApiCreateNetworkService(this,networkCreateMessage)
        apiCreateNetworkService.sendMessage()
    }

    def void messageListener(String message){
        println "===================================================================="
        this.messagingTemplate.convertAndSend("/topic/networkmessage", message)
    }

}
