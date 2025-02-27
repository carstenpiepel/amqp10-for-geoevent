# amqp10-for-geoevent

The AMPQ 1.0 Inbound Transport for ArcGIS GeoEvent Server connects GeoEvent Server with message brokers that are 
compliant with the AMQP 1.0 protocol. This transport has been tested with Azure 
Service Bus but it may also work with other message brokers including RabbitMQ 4.x, Azure Service Bus, ActiveMQ, and 
SwiftMQ. The transport uses the [SwiftMQ AMQP 1.0 Java Client](https://docs.swiftmq.com/sc/amqp-1-0-client) to connect 
to brokers. 

## Features
* AMQP 1.0 Inbound Transport

## Requirements

* ArcGIS GeoEvent Server.
* ArcGIS GeoEvent Server SDK.
* Java JDK 11 or later (the SwiftMQ client does not work with JDK 1.8).
* Maven 4.0 or later.

## Build

You can build the transport from the source code as described in this section, or you can use a pre-built release
version listed under [Releases > Tags](https://github.com/carstenpiepel/amqp10-for-geoevent/tags).

Building the source code:

1. Make sure Maven and ArcGIS GeoEvent Server SDK are installed on your machine.
2. Run 'mvn install -Dcontact.address=[YourContactEmailAddress]'

Installing the built jar files:

1. Copy the *.jar files under the 'target' sub-folder(s) into the [ArcGIS-GeoEvent-Server-Install-Directory]/deploy folder.

## Deploy and Configure

To deploy the transport and configure an input connector that uses the transport, follow these steps:

1. Sign into ArcGIS GeoEvent Manager.
2. Navigate to Site > Components > Transports.
3. Click on "Add Local Transport".
4. Choose the jar file containing the transport, and click on "Add". This will add the AMQP 1.0 Inbound Transport.
4. Navigate to Site > GeoEvent > Connectors.
5. Click on "Create Connector".
6. In the "Create Connector" dialog configure your connector as specified below:
    * Name: `amqp10-json-in`
    * Label: `Receive GeoJSON from an AMQP 1.0 Message Broker`
    * Description: `Receives GeoJSON from an AMQP 1.0 Message Broker`
    * Type: `Input`
    * Adapter: `GeoJSON`
    * Transport: `AMQP-1_0`
    * Default Input Name: `amqp10-geojson-in`
    * Shown Properties: List all properties except for "Use TLS/SSL"
    * Advanced Properties: None
    * Hidden Properties: "Use TLS/SSL"
7. Click on "Create". This will create the input connector.

![Create the AMQP 1.0 Inbound Connector](/assets/images/create_connector.png)

## Connect to Azure Service Bus

Follow these steps to connect GeoEvent Server to an existing Azure Service Bus queue or topic subscription: 

1. In ArcGIS GeoEvent Manager, navigate to the Manager tab.
2. Click on "Add Input". 
3. Under "AMQP 1.0 Inbound Transport", select the input connector you configured in the previous step, e.g.,
"Receive GeoJSON from an AMQP 1.0 Message Broker".
4. Use the "Add Input" dialog to configure the connection to Azure Service Bus. 
      * The first five configuration parameters work are typical parameters available for inputs. This includes the 
   name, GeoEvent definition name, the expected date format, and the default spatial reference.
      * Hostname: Specify the host name of the Azure Service Bus namespace in the format `<namespace>.servicebus.windows.net`, e.g., `mynamespace.servicebus.windows.net`.
      * Port: `5761`
      * Destination Type: `Queue` or `Topic`
      * Destination Name:
        * For a Queue: Use the queue name, e.g., `myqueue`.
        * For a Topic: Use the format `<topic>/Subscriptions/<subscription>`, e.g. `mytopic/Subscriptions/mysubscription`.
      * Authentication Required: `Yes`
      * SASL Authentication Type: `PLAIN`
      * Username: The name of a Shared Access Policy with the `Listen` claim for your queue or topic.
      * Password: The primary or secondary key for the Shared Access Policy.
5. Click on "Save" to create the input.

![Create the AMQP 1.0 Input](/assets/images/create_input.png)

## Other Resources

* [ArcGIS GeoEvent Server Resources](http://links.esri.com/geoevent)

## Issues

Find a bug or want to request a new feature?  Please let us know by submitting an issue.

## Contributing

Esri welcomes contributions from anyone and everyone. Please see our [guidelines for contributing](https://github.com/esri/contributing).

## Licensing
Copyright 2025 Esri

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

A copy of the license is available in the repository's [license.txt](license.txt?raw=true) file.


This fork has been modified to use SSL for incoming ActiveMQ queues.  To modifiy the outgoing to use SSL, you would
need to make similar changes in the outgoing transport and definition.
