package zstackui

import com.rabbitmq.client.*
import grails.core.GrailsApplication
import groovy.json.*
import java.util.*
import grails.transaction.Transactional
import com.jcabi.aspects.*
import com.jcabi.*

class AsyncMessageService {

    def UuidService

    def P2P_EXCHANGE, BROADCAST_EXCHANGE, REPLY_QUEUE_NAME, REQUEST_QUEUE_NAME, API_EVENT_QUEUE_PREFIX
    GrailsApplication grailsApplication
    QueueingConsumer consumer
    Channel channel
    JsonSlurper parser
    Connection connection
	def Host = "172.20.11.81"
	
	//message id
    def corrId

    def shareMap = [:]

	//?????exchange,channel,queue
    def initialize() {
        P2P_EXCHANGE = "P2P"
		BROADCAST_EXCHANGE = "BROADCAST"
        REQUEST_QUEUE_NAME = "zstack.message.api.portal"
		API_EVENT_QUEUE_PREFIX = "zstck.ui.api.event.%s"
		UuidService = new UuidService();
        REPLY_QUEUE_NAME = "zstack.newui.message." + UuidService.getUuid()
	
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(Host)
        Connection connection = factory.newConnection()
        Channel channel = connection.createChannel()
        this.channel = channel
        this.connection = connection

        def args = ["alternate-exchange": "NO_ROUTE"]
		
		try {
            channel.exchangeDeclarePassive(P2P_EXCHANGE);
        }catch (Exception e) {
        	channel.exchangeDeclare(P2P_EXCHANGE, "topic", true, false, args as Map<String, Object>)
        }
		
		try {
			channel.exchangeDeclarePassive(BROADCAST_EXCHANGE);
		}catch (Exception e) {
			channel.exchangeDeclare(BROADCAST_EXCHANGE, "topic", true, false, args as Map<String, Object>)
		}
		
        channel.queueDeclare(REPLY_QUEUE_NAME, false, false, true, null)
        channel.queueBind(REPLY_QUEUE_NAME, P2P_EXCHANGE, REPLY_QUEUE_NAME)
        this.consumer = new QueueingConsumer(channel)
		
    }
	
	AsyncMessageService(){
		this.P2P_EXCHANGE = "P2P"
		this.BROADCAST_EXCHANGE = "BROADCAST"
		this.REQUEST_QUEUE_NAME = "zstack.message.api.portal"
		this.UuidService = new UuidService();
		this.API_EVENT_QUEUE_PREFIX = "zstck.ui.api.event."+UuidService.getUuid()
		this.REPLY_QUEUE_NAME = "zstack.newui.message." + UuidService.getUuid()
		

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(Host)
		Connection connection = factory.newConnection()
		Channel channel = connection.createChannel()
		this.channel = channel
		this.connection = connection

		def args = ["alternate-exchange": "NO_ROUTE"]
		try {
			this.channel.exchangeDeclarePassive(P2P_EXCHANGE);
		}catch (Exception e) {
			this.channel.exchangeDeclare(P2P_EXCHANGE, "topic", true, false, args as Map<String, Object>)
		}
		
		try {
			channel.exchangeDeclarePassive(BROADCAST_EXCHANGE);
		}catch (Exception e) {
			channel.exchangeDeclare(BROADCAST_EXCHANGE, "topic", true, false, false, null)
		}
		
		this.channel.queueDeclare(REPLY_QUEUE_NAME, false, false, true, null)
		this.channel.queueBind(REPLY_QUEUE_NAME, P2P_EXCHANGE, REPLY_QUEUE_NAME)
		this.consumer = new QueueingConsumer(channel)
	}
	

	@Async
	def String send(String message) {
		
		parser = new JsonSlurper()
		corrId = UUID.randomUUID().toString().replace("-", "")
		
		if(!message){
			println "you cannot pass an empty message to me!"
			return "no message"
		}

		def obj = parser.parseText(message)
		def body = obj.values()[0]
		body.put("serviceId", "api.portal")
		body.put("id", corrId)
		
		println "request queue name is: "+this.REQUEST_QUEUE_NAME;
		println "exchange name is: "+this.P2P_EXCHANGE;
		println "corrid is: "+this.corrId;
		
		def bodyHeaders = ["replyTo": REPLY_QUEUE_NAME, "noReply": "false", "correlationId": corrId]
		body.put("headers", bodyHeaders)
		message = JsonOutput.toJson(obj)
		
		println "send message :"+message

		//???????
		BasicProperties props = new AMQP.BasicProperties.Builder()
				.correlationId(corrId)
				.replyTo(REPLY_QUEUE_NAME)
				.build()
		channel.basicPublish(P2P_EXCHANGE, REQUEST_QUEUE_NAME, props, message.getBytes("UTF-8"))

		Envelope envelope = new Envelope();
		
		shareMap = [corrId: envelope];
		//sync_call()

		messageHandler()
		
		println "message send success!"
		return "message send success!"
		
		//
	}

	def sync_call(){
		try{
			Envelope cosumerenvelope
			QueueingConsumer.Delivery delivery = consumer.nextDelivery()
			cosumerenvelope = new Envelope(new String(delivery.getBody()))
			shareMap[corrId] = cosumerenvelope
			synchronized (shareMap) {
				shareMap.notify();
			}
			channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			
		}catch (InterruptedException ie){
			System.out.pritnln("get message failed!");
			log.info "get message failed!"
		}
		
	}

	def closeChannel(){
		channel.close();
		connection.close()
	}

	public class Envelope{
		def msg;
		Envelope(){}
		Envelope(def msg){
			this.msg = msg
		}
	}

	@Async
	public messageHandler(){
		
		println("message receiving------------------------------------------------------------------")
		channel.basicConsume(REPLY_QUEUE_NAME, true, consumer)
		println("1111111111111111111111111111")
		Envelope cosumerenvelope
		println "2222222222222222222222"
		QueueingConsumer.Delivery delivery = consumer.nextDelivery()
		println "333333333333333"
		cosumerenvelope = new Envelope(new String(delivery.getBody()))
		
		shareMap[corrId] = cosumerenvelope
		channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		
		def obj = parser.parseText(shareMap[corrId].msg)
		def body = obj.values()[0]
		def bodyHeaders = body["headers"]
		if (bodyHeaders["isReply"] == "true" && bodyHeaders["correlationId"] == corrId) {
			println(JsonOutput.toJson(body))
			log.info JsonOutput.toJson(body)+""
			return JsonOutput.toJson(body)
		}
		closeChannel()
		
	}
}
