package zstackui



import grails.transaction.Transactional

import java.io.ObjectInputStream.ValidationList.Callback;

import com.rabbitmq.client.*

class AsyncConsumerService {

    def void getReply(CallBackService callBackService,Channel channel,Consumer consumer,String REPLY_QUEUE_NAME) {
		println "is receivng message..........................................................................."
		channel.basicConsume(REPLY_QUEUE_NAME, true, consumer)
		QueueingConsumer.Delivery delivery = consumer.nextDelivery()
		//channel.basicConsume(REPLY_QUEUE_NAME, true, consumer);
		callBackService.MessageHandler(new String(delivery.getBody()));
    }
}
