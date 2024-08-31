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

public class AMQP10DestinationInfo implements Validatable {
    private static final BundleLogger LOGGER = BundleLoggerFactory.getLogger(AMQP10InboundTransport.class);
    private final String type;
    private final String name;

    public AMQP10DestinationInfo(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public void validate() throws ValidationException {
        if (!"Queue".equalsIgnoreCase(type) && !"Topic".equalsIgnoreCase(type))
            throw new ValidationException(LOGGER.translate("DESTINATION_TYPE_VALIDATE_ERROR"));
        if (name == null || name.isEmpty())
            throw new ValidationException(LOGGER.translate("DESTINATION_NAME_VALIDATE_ERROR"));

    }
}