# amqp10-for-geoevent

The AMPQ 1.0 Inbound Transport for ArcGIS GeoEvent Server connects GeoEvent Server with message brokers that are 
compliant with the AMQP 1.0 protocol. This transport has been tested with Azure 
Service Bus but it may also work with other message brokers including RabbitMQ 4.x, Azure Service Bus, ActiveMQ, and 
SwiftMQ. The transport uses the [SwiftMQ AMQP 1.0 Java Client](https://docs.swiftmq.com/sc/amqp-1-0-client) to connect 
to brokers. 

## Features
* AMQP 1.0 Inbound Transport

## Instructions

Building the source code:

1. Make sure Maven and ArcGIS GeoEvent Server SDK are installed on your machine.
2. Run 'mvn install -Dcontact.address=[YourContactEmailAddress]'

Installing the built jar files:

1. Copy the *.jar files under the 'target' sub-folder(s) into the [ArcGIS-GeoEvent-Server-Install-Directory]/deploy folder.

## Requirements

* ArcGIS GeoEvent Server.
* ArcGIS GeoEvent Server SDK.
* Java JDK 11 or later (the SwiftMQ client does not work with JDK 1.8).
* Maven 4.0 or later.

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
