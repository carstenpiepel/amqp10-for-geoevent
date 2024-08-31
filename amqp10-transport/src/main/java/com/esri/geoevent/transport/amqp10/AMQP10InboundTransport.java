/*
  Copyright 1995-2015 Esri

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

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.component.RunningState;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.transport.InboundTransportBase;
import com.esri.ges.transport.TransportDefinition;

public class AMQP10InboundTransport extends InboundTransportBase {
    private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(AMQP10InboundTransport.class);
    private AMQP10ConnectionInfo connectionInfo;
    private AMQP10DestinationInfo destinationInfo;
    private AMQP10ConnectionService connectionService;
    private AMQP10ConsumerService consumerService;

    public AMQP10InboundTransport(TransportDefinition definition) throws ComponentException {
        super(definition);
    }

    public boolean isClusterable() {
        return true;
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public synchronized void start() {
        switch (getRunningState()) {
            case STOPPING:
            case STOPPED:
            case ERROR:
                doStart();
                break;
        }
    }

    @Override
    public synchronized void stop() {
        if (!RunningState.STOPPED.equals(getRunningState())) {
            doStop();
        }
    }

    @Override
    public void afterPropertiesSet() {
        doStop();
        String password;
        try {
            password = getProperty("password").getDecryptedValue();
        } catch (Exception e) {
            password = getProperty("password").getValueAsString();
        }

        String hostname = getProperty("hostname").getValueAsString();
        String port = getProperty("port").getValueAsString();
        String tls = getProperty("tls").getValueAsString();
        String authenticationRequired = getProperty("authenticationRequired").getValueAsString();
        String saslAuthenticationType = getProperty("saslAuthenticationType").getValueAsString();
        String username = getProperty("username").getValueAsString();

        connectionInfo = new AMQP10ConnectionInfo(hostname, port, tls, authenticationRequired, saslAuthenticationType, username, password);

        String destinationType = getProperty("destinationType").getValueAsString();
        String destinationName = getProperty("destinationName").getValueAsString();

        destinationInfo = new AMQP10DestinationInfo(destinationType, destinationName);

        super.afterPropertiesSet();
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        connectionInfo.validate();
        destinationInfo.validate();
    }

    private synchronized void doStart() {
        doStop();
        setRunningState(RunningState.STARTING);
        try {
            int timeout = 5000;
            if (connectionService == null)
                connectionService = new AMQP10ConnectionService(connectionInfo, timeout);
            connectionService.start();
            if (consumerService == null)
                consumerService = new AMQP10ConsumerService(
                        connectionService.getConnection(),
                        connectionService.getSession(),
                        destinationInfo,
                        byteListener,
                        timeout);
            consumerService.start();
        } catch (AMQP10TransportException e) {
            LOGGER.error("TRANSPORT_START_ERROR", e, e.getMessage());
            doStop(e.getMessage());
            setRunningState(RunningState.ERROR);
        }
    }

    private synchronized void doStop() {
        doStop(null);
    }

    private synchronized void doStop(String reason) {
        setRunningState(RunningState.STOPPING);
        setErrorMessage(reason);
        if (connectionService != null)
            try {
                connectionService.stop();
            } finally {
                connectionService = null;
            }
        if (consumerService != null)
            try {
                consumerService.stop();
            } finally {
                consumerService = null;
            }
        setRunningState(RunningState.STOPPED);
    }

    public void shutdown() {
        doStop();
        super.shutdown();
    }
}