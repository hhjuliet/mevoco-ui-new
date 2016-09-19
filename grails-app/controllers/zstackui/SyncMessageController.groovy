package zstackui

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo

class SyncMessageController {

	@Autowired
    SyncRabbitmqService syncRabbitmqService

	
	def message = '''{
  "org.zstack.header.identity.APILogInByAccountMsg": {
    "accountName": "admin",
    "password": "b109f3bbbc244eb82441917ed06d618b9008dd09b3befd1b5e07394c706a8bb980b1d7785e5976ec049b46df5f1326af5a2ea6d103fd07c95385ffab0cacbc86",
    "session": {
      "uuid": "47efe923b34a40d5908e038a06b29add"
    },
    "timeout": 1800000,
    "headers": {
      "correlationId": "c9e43b8a8f084ba3a4dc3d012c6cd197",
      "replyTo": "zstack.message.cloudbus.0b1d9f5fc2f0406eba60f67df1d5197a",
      "noReply": "false",
      "schema": {}
    },
    "id": "c9e43b8a8f084ba3a4dc3d012c6cd197",
    "serviceId": "api.portal",
    "createdTime": 1472524920907
  }
}'''
    def index() {
		def reply;
		
		if (message) {
			//println message
			reply = syncRabbitmqService.send(message)
		}else{
			reply = 'failed'
		}
		
		render reply
	}

    @MessageMapping("/sync")
    @SendTo("/topic/sync")
    protected String call(String msg) {
        /*println msg
        def reply = syncRabbitmqService.send(msg)
        return reply*/
    }

}
