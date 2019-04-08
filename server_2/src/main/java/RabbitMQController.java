import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class RabbitMQController {

}


//RabbitMQ part
/*
ConnectionFactory factory = new ConnectionFactory();
String amqpURL = ENV_AMQPURL_NAME != null ? ENV_AMQPURL_NAME : "amqp://localhost";
try {
	factory.setUri(amqpURL);
} catch (Exception e) {
	System.out.println(" [*] AQMP broker not found in " + amqpURL);
	System.exit(-1);
}
System.out.println(" [*] AQMP broker found in " + amqpURL);
Connection connection = null;
Channel channel = null;
try{
	connection = factory.newConnection();
	channel = connection.createChannel();
} catch (Exception e) {
	System.out.println(e.getMessage());
}

// Declaramos una cola en el broker a través del canal
// recién creado llamada QUEUE_NAME (operación
// idempotente: solo se creará si no existe ya)
// Se crea tanto en el emisor como en el receptor, porque no
// sabemos cuál se lanzará antes.
// Indicamos que no sea durable ni exclusiva

channel.queueDeclare(TEST_QUEUE, false, false, false, null);
*/

/*
channel.basicPublish("", TEST_QUEUE, null, message.getBytes());
channel.close();
connection.close();
*/