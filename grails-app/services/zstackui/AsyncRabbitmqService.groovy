package zstackui

import com.rabbitmq.client.*
import grails.core.GrailsApplication
import groovy.json.*

class AsyncRabbitmqService{

    static scope = "singleton"

    GrailsApplication grailsApplication

	def P2P_EXCHANGE, BROADCAST_EXCHANGE, REPLY_QUEUE_NAME, REQUEST_QUEUE_NAME,API_EVENT_QUEUE_PREFIX;
	def UuidService
    def lastMessage
	def connection
	def channel
	def parser
	//message correlative id
	def corrId
	def Host = "172.20.11.81"
	def consumer
	private final AsyncMessageController asyncMessageController
	private CallBackService callBackService
	private static test;
	def sendOrRollback;
	def API_EVENT_QUEUE_BINDING_KEY;

	def initialize(){}
	
	public AsyncRabbitmqService(){
		test = 0;

		//this.asyncMessageController = asyncMessageController;
		this.P2P_EXCHANGE = "P2P"
		this.REQUEST_QUEUE_NAME = "zstack.message.api.portal"
		this.UuidService = new UuidService();
		this.REPLY_QUEUE_NAME = "zstack.newui.api.event." + UuidService.getUuid()
		def api_event_queue_name = API_EVENT_QUEUE_PREFIX+UuidService.getUuid();
		this.BROADCAST_EXCHANGE = "BROADCAST"
		this.API_EVENT_QUEUE_BINDING_KEY = "key.event.API.API_EVENT"
		this.API_EVENT_QUEUE_PREFIX = "zstack.ui.api.event."

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(Host);
		
		this.connection = factory.newConnection();
		this.channel = connection.createChannel();
		
		String NO_ROUTE="NO_ROUTE";
		
		try {
			this.channel.exchangeDeclarePassive(P2P_EXCHANGE);
		} catch (Exception e) {
			this.channel.exchangeDeclare(P2P_EXCHANGE,"topic", true,false,false,["alternate-exchange":NO_ROUTE]);
		}
		
		try {
			this.channel.exchangeDeclarePassive(BROADCAST_EXCHANGE);
		}catch (Exception e) {
			this.channel.exchangeDeclare(BROADCAST_EXCHANGE, "topic", true, false, ["alternate-exchange":NO_ROUTE])
		}

		this.channel.queueDeclare(api_event_queue_name, false, false, true, null);
		this.channel.queueDeclare(REPLY_QUEUE_NAME, false, false, true, null);

		this.channel.queueBind(REPLY_QUEUE_NAME,P2P_EXCHANGE,REPLY_QUEUE_NAME)
		this.channel.queueBind(REPLY_QUEUE_NAME, BROADCAST_EXCHANGE, API_EVENT_QUEUE_BINDING_KEY);

		this.consumer = new QueueingConsumer(channel)
	}
	

    String sendMessage(CallBackService callBackService,String msg,UuidService uuidService,Boolean sendOrRollback){
		this.callBackService = callBackService;
		this.sendOrRollback = sendOrRollback;
		corrId = uuidService.getUuid();
		
		//System.out.println("original1 message is: " + msg);
		parser = new JsonSlurper();
		
		if(!msg){
			println "you cannot pass an empty message to me!"
			return "message is null"
		}
		
		//put queue name(routingkey) and messagecorrelative Id into headers
		def obj = parser.parseText(msg)
        def body = obj.values()[0]
        body.put("serviceId", "api.portal")
        body.put("id", corrId)
		def bodyHeaders = ["replyTo": REPLY_QUEUE_NAME, "noReply": "false", "correlationId": corrId]
        body.put("headers", bodyHeaders)
        msg = JsonOutput.toJson(obj)
		

		//publishing message
		BasicProperties props = new AMQP.BasicProperties.Builder()
				.correlationId(this.corrId)
				.replyTo(this.REPLY_QUEUE_NAME)
				.build()
		channel.basicPublish(P2P_EXCHANGE, REQUEST_QUEUE_NAME, props, msg.getBytes("UTF-8"))
		System.out.println(" [x] Sent '" + msg + "'");
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				println "is receivng message..........................................................................."
				channel.basicConsume(REPLY_QUEUE_NAME, true, consumer)
				QueueingConsumer.Delivery delivery = consumer.nextDelivery()
				println "message received!"
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				MessageHandler(new String(delivery.getBody()));
			}
		}).start();
		
		System.out.println("send message success!");
		System.out.println("=============================================================================================");
    }
	
	def void MessageHandler(String message){

		println "received message is :"+message;

		def jsonSlurper = new JsonSlurper()
		def object = jsonSlurper.parseText(message).values()[0]

		println "object is :"+object;
		println "object success is: "+object.success;

		if (sendOrRollback == false){
			println "rollbacking..."
			callBackService.failed(message);
		}else if (object.success == false){
			println "rollbacking..."
			callBackService.failed(message);
		}else if (object.success == true){
			println "rabbitmq receive message success..."
			callBackService.success(message);
		}else{
			println "unknown rabbitmq reply message"
		}
	}
}




