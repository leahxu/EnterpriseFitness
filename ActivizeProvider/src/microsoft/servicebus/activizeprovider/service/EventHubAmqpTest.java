package microsoft.servicebus.activizeprovider.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.qpid.amqp_1_0.client.AcknowledgeMode;
import org.apache.qpid.amqp_1_0.client.Connection;
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
import org.apache.qpid.amqp_1_0.type.messaging.Filter;
import org.apache.qpid.amqp_1_0.type.messaging.MessageAnnotations;

public class EventHubAmqpTest
{
    final static String connectionUri = "amqps://owner:<key>@namespace?sync-publish=true&max-sessions=10000";
    final static String consumerAddress = "eventhubname/consumergroupname/partition_number";
    
    public static void main(String args[]) throws ConnectionException, MalformedURLException
    {
        Logger logger = Logger.getLogger("FRM");
        logger.setLevel(Level.FINE);
        logger.addHandler(new Handler()
        {
            @Override
            public void publish(LogRecord record) {
                System.out.println(record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
        
        URL url = new URL(null, connectionUri, new URLStreamHandler()
        {
            @Override
            protected URLConnection openConnection(URL u) throws IOException
            {
                throw new UnsupportedOperationException();
            }
        });
        String protocol = url.getProtocol();
        boolean ssl = "amqps".equals(protocol); 
        String host = url.getHost();
        int port = url.getPort();
        if (port == -1) port = ssl ? 5671 : 5672;
        String userInfo = url.getUserInfo();
        String userName = null;
        String password = null;
        if (userInfo != null)
        {
            String[] creds = userInfo.split(":", 2);
            userName = URLDecoder.decode(creds[0]);
            password = URLDecoder.decode(creds[1]);
        }
        
        Connection connection = new Connection(host, port, userName, password, host, ssl);
        SelectorFilterWriter.register(connection.getEndpoint().getDescribedTypeRegistry());
        
        Session session = connection.createSession();
        Map<Symbol, Filter> filters = Collections.singletonMap(
                Symbol.valueOf("apache.org:selector-filter:string"),
                (Filter)new SelectorFilter("amqp.annotation.x-opt-offset > '-1'")); 
        Receiver receiver = session.createReceiver(
                consumerAddress,
                AcknowledgeMode.AMO,
                "eventhub-receiver-link",
                false,
                filters,
                null);
        receiver.setCredit(UnsignedInteger.valueOf(10), true);
                
        while (true) {
            Message message = receiver.receive(60000);
            if (message == null) {
                break;
            }
            
            for (Section s : message.getPayload())
            {
                if (s instanceof MessageAnnotations)
                {
                    System.out.print(((MessageAnnotations)s).getValue());
                }
                
                System.out.println();
            }
            
            receiver.acknowledge(message);
        }
        
        receiver.close();
        session.close();
        connection.close();
    }
    
    static class SelectorFilter implements Filter
    {
        private final String _value;

        public SelectorFilter(String value)
        {
            _value = value;
        }

        public String getValue()
        {
            return _value;
        }
        
        public String toString()
        {
            return _value;
        }
    }
    
    static class SelectorFilterWriter extends AbstractDescribedTypeWriter<SelectorFilter>
    {
        private SelectorFilter _value;

        public SelectorFilterWriter(final Registry registry)
        {
            super(registry);
        }

        @Override
        protected void onSetValue(final SelectorFilter value)
        {
            _value = value;
        }

        @Override
        protected void clear()
        {
            _value = null;
        }

        protected Object getDescriptor()
        {
            return UnsignedLong.valueOf(0x00000137000000AL);
        }

        @Override
        protected ValueWriter createDescribedWriter()
        {
            return getRegistry().getValueWriter(_value.getValue());
        }

        private static Factory<SelectorFilter> FACTORY = new Factory<SelectorFilter>()
        {

            public ValueWriter<SelectorFilter> newInstance(Registry registry)
            {
                return new SelectorFilterWriter(registry);
            }
        };

        public static void register(Registry registry)
        {
            registry.register(SelectorFilter.class, FACTORY);
        }
    }
}
