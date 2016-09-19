package zstackui
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

class TestController {

    def index() { }
	
	@Subscribe
	def String messageListener(MessageEvent event){
		/*System.out.println("Message:"+event.getMessage());
		System.out.println("received message :"+event.getMessage());
		return event.getMessage();*/
		println "string is :"+event.getMsg();
		return event.getMsg();
	}
}
