
/*
  Copyright 1995-2015  Esri

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  For additional information, contact:
  Environmental Systems Research Institute, Inc.
  Attn: Contracts Dept
  380 New York Street
  Redlands, California, USA 92373

  email: contracts@esri.com
*/

package com.esri.geoevent.transport.amqp10;

import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.swiftmq.amqp.AMQPContext;
import com.swiftmq.amqp.v100.client.*;
import com.swiftmq.net.JSSESocketFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AMQP10ConnectionService implements AMQP10Service {
    public static final int MAX_WAIT_TIME_MILLIS = 300000;
    private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(AMQP10InboundTransport.class);
    private final AMQP10ConnectionInfo connectionInfo;
    private final int timeout;
    private final AMQPContext ctx;
    private Connection connection;
    private Session session;
    private ScheduledExecutorService monitorService;
    private int retryCount;

    public AMQP10ConnectionService(AMQP10ConnectionInfo connectionInfo, int timeout) {
        this.connectionInfo = connectionInfo;
        this.timeout = timeout;
        retryCount = 0;
        ctx = new AMQPContext(AMQPContext.CLIENT);
    }

    public Connection getConnection() throws AMQP10TransportException {
        if (isRunning()) {
            return connection;
        }
        throw new AMQP10TransportException(LOGGER.translate("CONNECTION_SERVICE_NOT_RUNNING_ERROR", connectionInfo.getHostname()));
    }

    public Session getSession() throws AMQP10TransportException {
        if (isRunning()) {
            return session;
        }
        throw new AMQP10TransportException(LOGGER.translate("CONNECTION_SERVICE_NOT_RUNNING_ERROR", connectionInfo.getHostname()));
    }

    @Override
    public boolean isRunning() {
        return connection != null && session != null && monitorService != null && !monitorService.isShutdown();
    }

    @Override
    public synchronized void start() throws AMQP10TransportException {
        if (!isRunning()) {
            stop();
            Connection amqpConn;
            Session amqpSession;
            try {
                if (!connectionInfo.isAuthenticationRequired()) {
                    amqpConn = new Connection(ctx, connectionInfo.getHostname(), connectionInfo.getPort(), false);
                } else {
                    if ("ANONYMOUS".equalsIgnoreCase(connectionInfo.getSASLAuthenticationType())) {
                        amqpConn = new Connection(ctx, connectionInfo.getHostname(), connectionInfo.getPort(), true);
                    } else if ("PLAIN".equalsIgnoreCase(connectionInfo.getSASLAuthenticationType())) {
                        amqpConn = new Connection(ctx, connectionInfo.getHostname(), connectionInfo.getPort(), connectionInfo.getUsername(), connectionInfo.getPassword());
                        amqpConn.setMechanism("PLAIN");
                    } else {
                        throw new AMQP10TransportException("CONNECTION_SASL_AUTHENTICATION_TYPE_VALIDATE_ERROR");
                    }
                }
                if (connectionInfo.isTlsConnection()) {
                    amqpConn.setSocketFactory(new JSSESocketFactory());
                }
                amqpConn.setOpenHostname(connectionInfo.getHostname());
                amqpConn.setExceptionListener(new ExceptionListener() {
                    @Override
                    public void onException(Exception e) {
                        LOGGER.error("CONNECTION_ERROR", connectionInfo.getHostname(), e.getMessage(), e);
                        try {
                            start();
                        } catch (Exception ignored) {}
                    }
                });

                amqpConn.connect();

                LOGGER.info("CONNECTION_ESTABLISH_SUCCESS", connectionInfo.getHostname());
            } catch (Throwable th) {
                retryCount++;
                throw new AMQP10TransportException(LOGGER.translate("CONNECTION_ESTABLISH_ERROR", connectionInfo.getHostname(), th.getMessage()));
            }

            try {
                amqpSession = amqpConn.createSession(100, 100);
                LOGGER.info("SESSION_CREATE_SUCCESS", connectionInfo.getHostname());
            } catch (ConnectionClosedException | SessionHandshakeException e) {
                retryCount++;
                throw new AMQP10TransportException(LOGGER.translate("SESSION_CREATE_ERROR", connectionInfo.getHostname(), e.getMessage()));
            }

            connection = amqpConn;
            session = amqpSession;

            monitorService = Executors.newSingleThreadScheduledExecutor();
            monitorService.schedule(new Callable<Void>() {
                public Void call() {
                    try {
                        if (!isRunning()) {
                            try {
                                start();
                            } catch (Exception ignored) {
                                retryCount += 1;
                            }
                        } else {
                            retryCount = 0;
                        }
                    } finally {
                        long waitTime = (long) (Math.pow(2.0, (double) retryCount) * timeout);
                        if (waitTime <= MAX_WAIT_TIME_MILLIS)
                            monitorService.schedule(this, timeout, TimeUnit.MILLISECONDS);
                        else
                            monitorService.schedule(this, MAX_WAIT_TIME_MILLIS, TimeUnit.MILLISECONDS);
                    }
                    return null;
                }
            }, timeout, TimeUnit.MILLISECONDS);

            retryCount = 0;
        }
    }

    @Override
    public synchronized void stop() {
        try {
            Util.shutdownExecutorService(monitorService, timeout);
        } catch (Exception ignored) {
        } finally {
            monitorService = null;
        }

        if (session != null) {
            try {
                session.close();
            } catch (Exception ignored) {
            } finally {
                session = null;
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ignored) {
            } finally {
                connection = null;
            }
        }

        retryCount = 0;
    }
}
