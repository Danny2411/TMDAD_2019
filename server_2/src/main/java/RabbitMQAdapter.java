import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;



public class RabbitMQAdapter {

	private final static String ENV_AMQPURL_NAME  = "amqp://iiilqiyz:IB5oVZP1FEUICOlk9jpf7LsDrFynH-wQ@raven.rmq.cloudamqp.com/iiilqiyz";
	private final static String TEST_QUEUE = "HOLA";
	ConnectionFactory factory;
	Connection connection;
	Channel channel;
	QueueingConsumer consumer;
	
	public RabbitMQAdapter() {
		factory = new ConnectionFactory();
		String amqpURL = ENV_AMQPURL_NAME != null ? ENV_AMQPURL_NAME : "amqp://localhost";
		try {
			factory.setUri(amqpURL);
		} catch (Exception e) {
			System.out.println(" [*] AQMP broker not found in " + amqpURL);
			System.exit(-1);
		}
		System.out.println(" [*] AQMP broker found in " + amqpURL);
		connection = null;
		channel = null;
		try{
			connection = factory.newConnection();
			channel = connection.createChannel();
			consumer = new QueueingConsumer(channel);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void createQueue(String queue) {
		try {
			channel.queueDeclare(queue, false, false, false, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void publishMessage(String queue, String message) {
		if(checkNull()) {
			try {
				channel.basicPublish("", queue, null, message.getBytes());
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String consumeMessage(String queue) {
		if(checkNull()) {
			try {
				channel.basicConsume(queue, true, consumer);
				String message = null;
				QueueingConsumer.Delivery delivery = consumer.nextDelivery(1000);
				if(delivery == null) {
					return null;
				}
				message = new String(delivery.getBody());
				return message;
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
	
	public void removeQueue(String queue) {
		if(checkNull()) {
			try {
				channel.queueDelete(queue);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean checkNull() {
		if(factory != null && connection != null && channel != null) {
			return true;
		}
		return false;
	}
	
}


