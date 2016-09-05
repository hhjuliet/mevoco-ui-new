package zstackui

import com.rabbitmq.client.*
import grails.core.GrailsApplication
import groovy.json.*
import java.util.*
import grails.transaction.Transactional

@Transactional
class SyncRabbitmqService {

    static scope = "singleton"

    def UuidService

    String P2P_EXCHANGE, REPLY_QUEUE_NAME, REQUEST_QUEUE_NAME
    GrailsApplication grailsApplication
    QueueingConsumer consumer
    Channel channel
    JsonSlurper parser
    Connection connection
	
	//message id
    def corrId

    def shareMap = [:]

	//初始化exchange,channel,queue
    def initialize() {
        P2P_EXCHANGE = "P2P"
        REQUEST_QUEUE_NAME = "zstack.message.api.portal"
        REPLY_QUEUE_NAME = "zstack.newui.message." + UuidService.getUuid()

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(grailsApplication.config.getProperty("rabbitmq.host"))
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
		
        channel.queueDeclare(REPLY_QUEUE_NAME, false, false, true, null)
        channel.queueBind(REPLY_QUEUE_NAME, P2P_EXCHANGE, REPLY_QUEUE_NAME)
        this.consumer = new QueueingConsumer(channel)
        channel.basicConsume(REPLY_QUEUE_NAME, true, this.consumer)
    }
	
    def String send(String message) {
		
		parser = new JsonSlurper()
        corrId = UUID.randomUUID().toString().replace("-", "")
		
		if(!message){
			println "you cannot pass an empty message to me!"
			return "no message"
		}
		//消息头部放入返回消息的queue name(routingkey)和messageId
        def obj = parser.parseText(message)
        def body = obj.values()[0]
        body.put("serviceId", "api.portal")
        body.put("id", corrId)
        def bodyHeaders = ["replyTo": REPLY_QUEUE_NAME, "noReply": "false", "correlationId": corrId]
        body.put("headers", bodyHeaders)
        message = JsonOutput.toJson(obj)
		
		println "send message :"+message

		//发送消息
        BasicProperties props = new AMQP.BasicProperties.Builder()
                .correlationId(corrId)
                .replyTo(REPLY_QUEUE_NAME)
                .build()
        channel.basicPublish(P2P_EXCHANGE, REQUEST_QUEUE_NAME, props, message.getBytes("UTF-8"))

        Envelope envelope = new Envelope();
		
        shareMap = [corrId: envelope];
        sync_call()

        synchronized (shareMap) {
            while (!shareMap[corrId]) {
                shareMap.wait();
            }
			
            obj = parser.parseText(shareMap[corrId].msg)
            body = obj.values()[0]
            bodyHeaders = body["headers"]
            if (bodyHeaders["isReply"] == "true" && bodyHeaders["correlationId"] == corrId) {
                println(JsonOutput.toJson(body))
                log.info JsonOutput.toJson(body)+""
                return JsonOutput.toJson(body)
            }
			closeChannel()
        }


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

	//以后还可能有别的属性，先只有一个msg
    public class Envelope{
        def msg;
        Envelope(){}
        Envelope(def msg){
            this.msg = msg
        }
    }
}


