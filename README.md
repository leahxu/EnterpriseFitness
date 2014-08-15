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

ActivizeUI
------------
The landing page of the website (http://activizeweb.cloudapp.net) is a Leaderboard of the teams based on Total Steps, one of the metrics received from the wearable. I used bootstrap to create this table, and it is updated every time the page is refreshed. The “Leaderboard” tab as well as the “Activize” tab will take you to this page.

If you are logged in as a valid user, the “User” tab will show you each of your five real-time metrics on a different bar. If you have given the website your goals (“how to” explained later), each tab will tell you if you have reached your goal and, if not, how far you have to go before reaching it.

If you click any of these five colored bars, it will take you to its corresponding page which graphs your history of that metric over the last 30 days.

Still logged in, the “Team” tab will show you your team’s activity level (based on progress towards a Total Steps goal), how many team members are contributing to the team averages, and each of the five real-time metrics.

Similar to User, if you click any of the five colored bars, you will see a corresponding history trend for the last 30 days.
The “Update Goals” tab is to create or update your individual goals. Input values into each box and click “Submit”. You will see the changes made in the “User” tab. If you don’t want to change your goals, but simply want to view them, click the “View Goals” link at the bottom of the page.
To view these things, you can log in with these credentials.

username: Bill1
password: password

Authors
-----------------
* Bridget Davis
* Amna Hashmi
* Leah Xu

Microsoft Explorer Internship Summer 2014

With Contributations and Thanks from Microsoft Event Hubs, Apache Storm, Jimmy Campbell


