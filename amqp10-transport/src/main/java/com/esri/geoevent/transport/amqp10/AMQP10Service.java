package com.esri.geoevent.transport.amqp10;

public interface AMQP10Service {
    boolean isRunning();
    void start() throws AMQP10TransportException;
    void stop() throws AMQP10TransportException;
}
