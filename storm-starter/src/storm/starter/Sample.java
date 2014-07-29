package storm.starter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Random;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

public class Sample implements MessageListener {
	private static boolean runReceiver = true;
	private Connection connection;
	private Session sendSession;
	private Session receiveSession;
	private MessageProducer sender;
	private MessageConsumer receiver;
	private static Random randomGenerator = new Random();

	public Sample() throws Exception {
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.apache.qpid.amqp_1_0.jms.jndi.PropertiesFileInitialContextFactory");
		env.put(Context.PROVIDER_URL,
				"/home/leah/git/EnterpriseFitness/storm-starter/config.properties");
		Context context = new InitialContext(env);
		// Lookup ConnectionFactory and Queue
		ConnectionFactory cf = (ConnectionFactory) context.lookup("sb_string");
		Destination queue = (Destination) context.lookup("queue");
		// Create Connection
		connection = cf.createConnection();

		// Create sender-side Session and MessageProducer
		sendSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		sender = sendSession.createProducer(queue);
		if (runReceiver) {
			// Create receiver-side Session, MessageConsumer,and MessageListener
			receiveSession = connection.createSession(false,
					Session.CLIENT_ACKNOWLEDGE);
			receiver = receiveSession.createConsumer(queue);
			receiver.setMessageListener(this);
			connection.start();
		}
	}

	public static void main(String[] args) {
		try {
			if ((args.length > 0) && args[0].equalsIgnoreCase("sendonly")) {
				runReceiver = false;
			}
			Sample simpleSenderReceiver = new Sample();
			System.out
					.println("Press [enter] to send a message. Type 'exit' + [enter] to quit.");
			BufferedReader commandLine = new java.io.BufferedReader(
					new InputStreamReader(System.in));
			while (true) {
				String s = commandLine.readLine();
				if (s.equalsIgnoreCase("exit")) {
					simpleSenderReceiver.close();
					System.exit(0);
				} else {
					simpleSenderReceiver.sendMessage();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendMessage() throws JMSException {
		TextMessage message = sendSession.createTextMessage();
		long randomMessageID = randomGenerator.nextLong() >>> 1;
		message.setJMSMessageID("ID:" + randomMessageID);
		message.setText("Test AMQP message from JMS " + randomMessageID);
		sender.send(message);
		System.out.println("Sent message with JMSMessageID = "
				+ message.getJMSMessageID());
	}

	public void close() throws JMSException {
		connection.close();
	}

	public void onMessage(Message message) {
		try {
			System.out.println("Received message with JMSMessageID = "
					+ message.getJMSMessageID());
			System.out.println("Message  - "
					+ ((TextMessage) message).getText());
			message.acknowledge();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
