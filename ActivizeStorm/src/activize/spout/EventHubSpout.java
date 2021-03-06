package activize.spout;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.Map;

import org.apache.qpid.amqp_1_0.client.AcknowledgeMode;
import org.apache.qpid.amqp_1_0.client.Connection;
import org.apache.qpid.amqp_1_0.client.ConnectionErrorException;
import org.apache.qpid.amqp_1_0.client.ConnectionException;
import org.apache.qpid.amqp_1_0.client.Message;
import org.apache.qpid.amqp_1_0.client.Receiver;
import org.apache.qpid.amqp_1_0.client.Session;
import org.apache.qpid.amqp_1_0.codec.AbstractDescribedTypeWriter;
import org.apache.qpid.amqp_1_0.codec.ValueWriter;
import org.apache.qpid.amqp_1_0.type.Section;
import org.apache.qpid.amqp_1_0.type.Symbol;
import org.apache.qpid.amqp_1_0.type.UnsignedInteger;
import org.apache.qpid.amqp_1_0.type.UnsignedLong;
import org.apache.qpid.amqp_1_0.type.messaging.AmqpValue;
import org.apache.qpid.amqp_1_0.type.messaging.Filter;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

@SuppressWarnings("serial")
public class EventHubSpout extends BaseRichSpout {
	public static final String CONFIG_PREFETCH_COUNT = "amqp.prefetch.count";
	private static final long DEFAULT_PREFETCH_COUNT = 100;

	// Time in milliseconds to wait for a message from the queue if there is no
	// message ready when the topology requests a tuple (via {@link #nextTuple()}).
	public static final long WAIT_FOR_NEXT_MESSAGE = 1L;

	// Time in milliseconds to wait after losing connection to the AMQP broker
	// before attempting to reconnect.
	public static final long WAIT_AFTER_SHUTDOWN_SIGNAL = 10000L;

	private String connectionUri;
	private String consumerAddress;

	private String amqpHost;
	private int amqpPort;
	private String amqpUsername;
	private String amqpPassword;

	private transient Connection amqpConnection;
	private transient Session amqpSession;
	private transient Receiver receiver;

	private SpoutOutputCollector collector;

	public EventHubSpout(String connectionUri, String consumerAddress) {
		this.connectionUri = connectionUri;
		this.consumerAddress = consumerAddress;
		this.amqpHost = null;
		this.amqpPort = -1;
		this.amqpUsername = null;
		this.amqpPassword = null;
	}

	@SuppressWarnings("deprecation")
	public void open(Map config, TopologyContext context, SpoutOutputCollector collector) {
		this.collector = collector;
		Long prefetchCount = (Long) config.get(CONFIG_PREFETCH_COUNT);
		
		if (prefetchCount == null) {
			prefetchCount = DEFAULT_PREFETCH_COUNT;
		} else if (prefetchCount < 1) {
			throw new IllegalArgumentException(CONFIG_PREFETCH_COUNT
					+ " must be at least 1");
		}

		URL url = null;
		try {
			url = new URL(null, connectionUri, new URLStreamHandler() {
				@Override
				protected URLConnection openConnection(URL u)
						throws IOException {
					throw new UnsupportedOperationException();
				}
			});
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		String protocol = url.getProtocol();
		boolean ssl = "amqps".equals(protocol);
		amqpHost = url.getHost();
		amqpPort = url.getPort();
		if (amqpPort == -1)
			amqpPort = ssl ? 5671 : 5672;
		String userInfo = url.getUserInfo();
		amqpUsername = null;
		amqpPassword = null;
		if (userInfo != null) {
			String[] creds = userInfo.split(":", 2);
			amqpUsername = URLDecoder.decode(creds[0]);
			amqpPassword = URLDecoder.decode(creds[1]);
		}

		try {
			amqpConnection = new Connection(amqpHost, amqpPort, amqpUsername,
					amqpPassword, amqpHost, ssl);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
		SelectorFilterWriter.register(amqpConnection.getEndpoint()
				.getDescribedTypeRegistry());

		try {
			amqpSession = amqpConnection.createSession();
		} catch (ConnectionException e) {
			e.printStackTrace();
		}

		Map<Symbol, Filter> filters = Collections.singletonMap(Symbol
				.valueOf("apache.org:selector-filter:string"),
				(Filter) new SelectorFilter(
						"amqp.annotation.x-opt-offset > '-1'"));

		try {
			System.out.println(consumerAddress);
			receiver = amqpSession.createReceiver(consumerAddress,
					AcknowledgeMode.AMO, "eventhub-receiver-link", false,
					filters, null);
		} catch (ConnectionErrorException e) {
			e.printStackTrace();
		}
		receiver.setCredit(UnsignedInteger.valueOf(10), true);
	}

	public void fail() {
		//TODO Implement fail
	}

	public void nextTuple() {
		Message message = receiver.receive();
		if (message != null) {
			AmqpValue val = null;

			for (Section section : message.getPayload()) {
				if (section instanceof AmqpValue) {
					val = (AmqpValue) section;
				}
			}

			receiver.acknowledge(message);
			collector.emit(new Values((val.toString())));
		}
	}

	public void close() {
		receiver.close();
		amqpSession.close();
		try {
			amqpConnection.close();
		} catch (ConnectionErrorException e) {
			e.printStackTrace();
		}
	}

	static class SelectorFilter implements Filter {
		private final String _value;

		public SelectorFilter(String value) {
			_value = value;
		}

		public String getValue() {
			return _value;
		}

		public String toString() {
			return _value;
		}
	}

	static class SelectorFilterWriter extends
			AbstractDescribedTypeWriter<SelectorFilter> {
		private SelectorFilter _value;

		public SelectorFilterWriter(final Registry registry) {
			super(registry);
		}

		@Override
		protected void onSetValue(final SelectorFilter value) {
			_value = value;
		}

		@Override
		protected void clear() {
			_value = null;
		}

		protected Object getDescriptor() {
			return UnsignedLong.valueOf(0x00000137000000AL);
		}

		@Override
		protected ValueWriter<String> createDescribedWriter() {
			return getRegistry().getValueWriter(_value.getValue());
		}

		private static Factory<SelectorFilter> FACTORY = new Factory<SelectorFilter>() {

			public ValueWriter<SelectorFilter> newInstance(Registry registry) {
				return new SelectorFilterWriter(registry);
			}
		};

		public static void register(Registry registry) {
			registry.register(SelectorFilter.class, FACTORY);
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("message"));
	}
}
