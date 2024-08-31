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

import com.esri.ges.core.validation.Validatable;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.util.Converter;

public class AMQP10ConnectionInfo implements Validatable {
    private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(AMQP10InboundTransport.class);
    private final String hostname;
    private final int port;
    private final boolean tls;
    private final boolean authenticationRequired;
    private final String saslAuthenticationType;
    private final String username;
    private final String password;

    public AMQP10ConnectionInfo(String hostname, String port, String tls, String authenticationRequired, String saslAuthenticationType, String username, String password) {
        this.hostname = hostname;
        this.port = Converter.convertToInteger(port, 5671);
        this.tls = Converter.convertToBoolean(tls, true);
        this.authenticationRequired = Converter.convertToBoolean(authenticationRequired, false);
        this.saslAuthenticationType = saslAuthenticationType;
        this.username = username;
        this.password = password;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public boolean isTlsConnection() {
        return tls;
    }

    public boolean isAuthenticationRequired() {
        return authenticationRequired;
    }

    public String getSASLAuthenticationType() {
        return saslAuthenticationType;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void validate() throws ValidationException {
        if (hostname == null || hostname.isEmpty())
            throw new ValidationException(LOGGER.translate("CONNECTION_HOST_VALIDATE_ERROR"));
        if (port <= 0)
            throw new ValidationException(LOGGER.translate("CONNECTION_PORT_VALIDATE_ERROR"));
        if (port == 5672 && tls)
            LOGGER.warn("CONNECTION_PORT_AND_TLS_WARNING");
        if (authenticationRequired) {
            if (!"PLAIN".equalsIgnoreCase(saslAuthenticationType) && !"ANONYMOUS".equalsIgnoreCase(saslAuthenticationType))
                throw new ValidationException(LOGGER.translate("CONNECTION_SASL_AUTHENTICATION_TYPE_VALIDATE_ERROR"));
            if ("PLAIN".equalsIgnoreCase(saslAuthenticationType) && (username == null || username.isEmpty()))
                throw new ValidationException(LOGGER.translate("CONNECTION_USERNAME_VALIDATE_ERROR"));
            if ("PLAIN".equalsIgnoreCase(saslAuthenticationType) && (password == null || password.isEmpty()))
                throw new ValidationException(LOGGER.translate("CONNECTION_PASSWORD_VALIDATE_ERROR"));
        }
    }
}