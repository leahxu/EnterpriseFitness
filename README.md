Activize: Enterprise Fitness with IOT
=================

Getting Started
-----------------
See https://github.com/apache/incubator-storm to install Storm

Clone the Repository

    git clone https://github.com/leahxu/EnterpriseFitness.git

Download Maven and build your project.

    cd EnterpriseFitness
    mvn clean install -DskipTests
    mvn package

cd into your apache-storm/bin directory 

There are two topologies, one that runs with Service Bus, the other with EventHubs. The one that runs with Service Bus is called SBActivizeTopology, the one that runs with Event Hubs is called EHActivizeTopology

Build with either 

    ./storm jar EnterpriseFitness/storm-starter/target/activize-0.9.3-incubating-SNAPSHOT-jar-with-dependencies.jar activize.EHActivizeTopology
    ./storm jar EnterpriseFitness/storm-starter/target/activize-0.9.3-incubating-SNAPSHOT-jar-with-dependencies.jar activize.SBActivizeTopology

or 

    mvn compile exec:java -Dstorm.topology=storm.starter.EHActivizeTopology
    mvn compile exec:java -Dstorm.topology=storm.starter.SBActivizeTopology

Authors
-----------------
Amna Hashmi
Bridget Davis
Leah Xu

Microsoft Explorer Internship Summer 2014

With Contributations and Thanks from Microsoft Event Hubs, Apache Storm, Jimmy Campbell


