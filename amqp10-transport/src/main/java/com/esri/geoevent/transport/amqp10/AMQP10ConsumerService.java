package com.esri.geoevent.transport.amqp10;

import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.messaging.ByteListener;
import com.swiftmq.amqp.v100.client.*;
import com.swiftmq.amqp.v100.generated.messaging.message_format.AmqpValue;
import com.swiftmq.amqp.v100.messaging.AMQPMessage;
import com.swiftmq.amqp.v100.types.AMQPBinary;
import com.swiftmq.amqp.v100.types.AMQPString;
import com.swiftmq.amqp.v100.types.AMQPType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AMQP10ConsumerService implements AMQP10Service {

    public static final int MAX_WAIT_TIME_MILLIS = 300000;
    private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(AMQP10InboundTransport.class);
    private final Connection connection;
    private final Session session;
    private final AMQP10DestinationInfo destinationInfo;
    private final ByteListener byteListener;
    private Consumer consumer;
    private final String channelId;
    private final int timeout;
    private ScheduledExecutorService receiverService;
    private int retryCount;

    public AMQP10ConsumerService(Connection connection, Session session, AMQP10DestinationInfo destinationInfo, ByteListener byteListener, int timeout) {
        this.connection = connection;
        this.session = session;
        this.destinationInfo = destinationInfo;
        this.byteListener = byteListener;
        this.timeout = timeout;
        // create a channel id per instance
        channelId = UUID.randomUUID().toString();
        retryCount = 0;
    }

    @Override
    public boolean isRunning() {
        return connection != null && session != null && receiverService != null && !receiverService.isShutdown();
    }

    @Override
    public synchronized void start() throws AMQP10TransportException {
        if (!isRunning()) {
            try {
                // Azure Service Bus does not appear to support the no-local-filter
                consumer = session.createConsumer(destinationInfo.getName(), QoS.AT_LEAST_ONCE, false, null);
                LOGGER.info("CONSUMER_CREATE_SUCCESS", connection.getOpenHostname(), destinationInfo.getType(), destinationInfo.getName());
            } catch (AMQPException e) {
                throw new AMQP10TransportException(LOGGER.translate("CONSUMER_CREATE_ERROR", connection.getOpenHostname(), destinationInfo.getType(), destinationInfo.getName(), e.getMessage()));
            }
            receiverService = Executors.newSingleThreadScheduledExecutor();
            receiverService.schedule(new Callable<Void>() {
                public Void call() {
                    try {
                        if (isRunning()) {
                            byte[] bytes = receive(timeout);
                            if (bytes != null && bytes.length > 0) {
                                ByteBuffer bb = ByteBuffer.allocate(bytes.length);
                                bb.put(bytes);
                                bb.flip();
                                byteListener.receive(bb, channelId);
                                bb.clear();
                            }
                        } else {
                            retryCount++;
                            LOGGER.error("CONSUMER_SERVICE_NOT_RUNNING_ERROR", connection.getOpenHostname(), destinationInfo.getType(), destinationInfo.getName());
                            stop();
                            start();
                        }
                    } catch (Exception e) {
                        retryCount++;
                        LOGGER.info("CONSUMER_RECEIVE_ERROR", connection.getOpenHostname(), destinationInfo.getType(), destinationInfo.getName(), e.getMessage());
                    } finally {
                        long waitTime = (long) (Math.pow(2.0, (double) retryCount) * timeout);
                        if (waitTime <= MAX_WAIT_TIME_MILLIS)
                            receiverService.schedule(this, timeout, TimeUnit.MILLISECONDS);
                        else
                            receiverService.schedule(this, MAX_WAIT_TIME_MILLIS, TimeUnit.MILLISECONDS);

                    }
                    return null;
                }
            }, timeout, TimeUnit.MILLISECONDS);

            retryCount = 0;
        } else {
            retryCount++;
            throw new AMQP10TransportException(LOGGER.translate("CONNECTION_NO_CONNECTION_OR_SESSION", connection.getOpenHostname()));
        }
    }

    @Override
    public synchronized void stop() {
        try {
            Util.shutdownExecutorService(receiverService, timeout);
        } catch (Exception ignored) {
        } finally {
            receiverService = null;
        }

        if (consumer != null) {
            try {
                consumer.close();
            } catch (AMQPException ignored) {
            } finally {
                consumer = null;
            }
        }
        retryCount = 0;
    }

    private byte[] receive(int timeout) {
        AMQPMessage message = consumer.receive(timeout);
        if (message != null) {
            AmqpValue value = message.getAmqpValue();
            assert value != null;
            AMQPType type = value.getValue();
            assert type != null;
            if (type instanceof AMQPString) {
                AMQPString textMessage = (AMQPString) type;
                byte[] bytes = {};
                try {
                    bytes = textMessage.getValue().getBytes(StandardCharsets.UTF_8);
                } catch (Throwable error) {
                    LOGGER.error("MESSAGE_DECODING_ERROR", error, error.getMessage());
                }
                return bytes;
            } else if (type instanceof AMQPBinary) {
                AMQPBinary bytesMessage = (AMQPBinary) type;
                return bytesMessage.getValue();
            } else {
                LOGGER.error("MESSAGE_UNEXPECTED_TYPE", type.getName());
            }
        }
        return null;
    }
}
