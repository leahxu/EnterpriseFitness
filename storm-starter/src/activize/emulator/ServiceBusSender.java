package activize.emulator;

import java.util.Hashtable;
import java.util.Random;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

public class ServiceBusSender implements MessageListener {
    private Connection connection;
    private Session sendSession;
    private MessageProducer sender;
    private static Random randomGenerator = new Random();

    public ServiceBusSender() throws Exception {
        // Configure JNDI environment
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, 
                "org.apache.qpid.amqp_1_0.jms.jndi.PropertiesFileInitialContextFactory");
        env.put(Context.PROVIDER_URL, "config.properties");
        Context context = new InitialContext(env);

        // Lookup ConnectionFactory and Queue
        ConnectionFactory cf = (ConnectionFactory) context.lookup("SBCF");
        Destination queue = (Destination) context.lookup("SBQUEUE");

        // Create Connection
        connection = cf.createConnection();

        // Create sender-side Session and MessageProducer
        sendSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        sender = sendSession.createProducer(queue);
    }

    public static void main(String[] args) {        
        try {
        	ServiceBusSender simpleSenderReceiver = new ServiceBusSender();
            DeviceEmulator emulator = new DeviceEmulator(); 
            emulator.createDevices(); 
            
            for (String device : emulator.mockPedometer) {
            	simpleSenderReceiver.sendMessage(device);
            }
            
            simpleSenderReceiver.close();
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
        System.out.println("Sent = " + message.getText());
    }

    public void close() throws JMSException {
        connection.close();
    }

    @Override
	public void onMessage(Message message) {
		// Do nothing
	}
}   