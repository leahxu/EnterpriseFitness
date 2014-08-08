package activize.emulator;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;

import java.util.Hashtable;
import java.util.Random;

public class EventHubSender implements MessageListener {
    private Connection connection;
    private Session sendSession;
    private MessageProducer sender;
    private static Random randomGenerator = new Random();

    public EventHubSender() throws Exception {
        // Configure JNDI environment
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, 
                "org.apache.qpid.amqp_1_0.jms.jndi.PropertiesFileInitialContextFactory");
        env.put(Context.PROVIDER_URL, "config.properties");
        Context context = new InitialContext(env);

        // Lookup ConnectionFactory and Queue
        ConnectionFactory cf = (ConnectionFactory) context.lookup("EHPublisher");
        Destination queue = (Destination) context.lookup("EHSend");

        // Create Connection
        connection = cf.createConnection();

        // Create sender-side Session and MessageProducer
        sendSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        sender = sendSession.createProducer(queue);
    }

    public static void main(String[] args) {
        try {
            EventHubSender sender = new EventHubSender();
            DeviceEmulator emulator = new DeviceEmulator(); 
            emulator.createDevices(); 
            
            for (String device : emulator.mockPedometer) {
            	sender.sendMessage(device);
            }
            
            sender.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String input) throws JMSException {
        TextMessage message = sendSession.createTextMessage();
        message.setText(input);
        long randomMessageID = randomGenerator.nextLong() >>>1;
        message.setJMSMessageID("ID:" + randomMessageID);
        sender.send(message);
        System.out.println("Sent message with JMSMessageID = " + message.getText());
    }

    public void close() throws JMSException {
        connection.close();
    }

	@Override
	public void onMessage(Message arg0) {
		// TODO Auto-generated method stub
		
	}
}   