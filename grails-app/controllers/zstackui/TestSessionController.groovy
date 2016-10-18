package zstackui

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate

class TestSessionController {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public TestSessionController(SimpMessagingTemplate messagingTemplate){
        this.messagingTemplate = messagingTemplate
    }


    void setResults(String ret, String sessionId)
    {
        this.messagingTemplate.convertAndSend("/topic/testwsresponse/" + sessionId, ret);
    }

    @MessageMapping(value="/testws/{sessionId}")
    public void handleTestWS(@DestinationVariable String sessionId, @Payload String msg) throws InterruptedException
    {
        println "coming...";
        setResults("Testing Return", sessionId);
    }

}
