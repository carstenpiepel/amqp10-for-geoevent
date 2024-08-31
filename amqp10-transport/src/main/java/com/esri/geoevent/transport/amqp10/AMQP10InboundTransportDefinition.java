/*
  Copyright 1995-2013 Esri

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

import com.esri.ges.core.property.LabeledValue;
import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.transport.TransportDefinitionBase;
import com.esri.ges.transport.TransportType;

import java.util.ArrayList;
import java.util.List;

public class AMQP10InboundTransportDefinition extends TransportDefinitionBase {
  private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(AMQP10InboundTransport.class);

  public AMQP10InboundTransportDefinition() {
    super(TransportType.INBOUND);
    try {
      propertyDefinitions.put("hostname", new PropertyDefinition("hostname", PropertyType.String, null, "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_HOSTNAME_LBL}", "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_HOSTNAME_DESC}", true, false));
      propertyDefinitions.put("port", new PropertyDefinition("port", PropertyType.Integer, 5671, "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_PORT_LBL}", "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_PORT_DESC}", true, false));
      propertyDefinitions.put("tls", new PropertyDefinition("tls", PropertyType.Boolean, true, "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_TLS_LBL}", "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_TLS_DESC}", true, false));
      List<LabeledValue> allowedDestinationTypeValues = new ArrayList<>(2);
      allowedDestinationTypeValues.add(new LabeledValue("Queue", "Queue"));
      allowedDestinationTypeValues.add(new LabeledValue("Topic", "Topic"));
      propertyDefinitions.put("destinationType", new PropertyDefinition("destinationType", PropertyType.String, "Queue", "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_DESTINATION_TYPE_LBL}", "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_DESTINATION_TYPE_DESC}", true, false, allowedDestinationTypeValues));
      propertyDefinitions.put("destinationName", new PropertyDefinition("destinationName", PropertyType.String, null, "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_DESTINATION_NAME_LBL}", "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_DESTINATION_NAME_DESC}", true, false));
      propertyDefinitions.put("authenticationRequired", new PropertyDefinition("authenticationRequired", PropertyType.Boolean, false, "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_AUTHENTICATION_REQUIRED_LBL}", "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_AUTHENTICATION_REQUIRED_DESC}", true, false));
      List<LabeledValue> allowedSASLAuthenticationTypeValues = new ArrayList<>(2);
      allowedSASLAuthenticationTypeValues.add(new LabeledValue("ANONYMOUS", "ANONYMOUS"));
      allowedSASLAuthenticationTypeValues.add(new LabeledValue("PLAIN", "PLAIN"));
      propertyDefinitions.put("saslAuthenticationType", new PropertyDefinition("saslAuthenticationType", PropertyType.String, "ANONYMOUS", "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_SASL_AUTHENTICATION_TYPE_LBL}", "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_SASL_AUTHENTICATION_TYPE_DESC}", "authenticationRequired=true",false, false, allowedSASLAuthenticationTypeValues));
      propertyDefinitions.put("username", new PropertyDefinition("username", PropertyType.String, null, "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_USERNAME_LBL}", "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_USERNAME_DESC}", "saslAuthenticationType=PLAIN", false, false));
      propertyDefinitions.put("password", new PropertyDefinition("password", PropertyType.Password, null, "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_PASSWORD_LBL}", "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_PASSWORD_DESC}", "saslAuthenticationType=PLAIN",false, false));
    } catch (PropertyException error) {
      String errorMsg = LOGGER.translate("IN_INIT_ERROR", error.getMessage());
      LOGGER.error(errorMsg, error);
      throw new RuntimeException(errorMsg, error);
    }
  }

  @Override
  public String getName() {
    return "AMQP-1_0";
  }

  @Override
  public String getDomain() {
    return "com.esri.geoevent.transport.inbound";
  }


  @Override
  public String getLabel() {
    return "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_LABEL}";
  }

  @Override
  public String getDescription() {
    return "${com.esri.geoevent.transport.amqp10-transport.TRANSPORT_IN_DESC}";
  }
}
