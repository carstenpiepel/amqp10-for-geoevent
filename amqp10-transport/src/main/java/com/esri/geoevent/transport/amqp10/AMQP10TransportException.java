package com.esri.geoevent.transport.amqp10;

public class AMQP10TransportException extends Exception
{
    private static final long serialVersionUID = -1L;

    public AMQP10TransportException(String message)
    {
        super(message);
    }

    public AMQP10TransportException(String message, Throwable cause)
    {
        super(message, cause);
    }
}