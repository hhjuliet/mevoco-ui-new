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

	//��ʼ��exchange,channel,queue
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
		//��Ϣͷ�����뷵����Ϣ��queue name(routingkey)��messageId
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

		//������Ϣ
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

	//�Ժ󻹿����б�����ԣ���ֻ��һ��msg
    public class Envelope{
        def msg;
        Envelope(){}
        Envelope(def msg){
            this.msg = msg
        }
    }
}


