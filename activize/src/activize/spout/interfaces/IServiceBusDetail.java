package activize.spout.interfaces;

import activize.spout.ServiceBusSpoutException;

public interface IServiceBusDetail {
    public String getConnectionString() throws ServiceBusSpoutException;
    public String getNextMessageForSpout() throws ServiceBusSpoutException;
    public Boolean isConnected();
    public void connect() throws ServiceBusSpoutException;
}