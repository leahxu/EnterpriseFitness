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

Activize Provider and Consumer
-----------------

The architecture of a Samsung wearable device is that it works in conjunction with a paired mobile host device (such as a phone or tablet), on which it depends for certain services.  The host device runs an application called the Gear Manager, which communicates with the WearableManagerService running on the Gear.  Applications intended for Gear has two parts, one of which runs on the host, called the Host-side Application. This installed application will include an application widget intended for the wearable device, called the Wearable-side widget. The Host-side application generally serves a "provider" function, while the Wearable-side widget generally serves a "consumer" function (these functions will be described in more detail later). During the installation of an Host-side application which contains a Wearable-side widget, the Gear Manager sends the Wearable-side widget to the WearableManagerService on the wearable. The WearableManagerService then installs the Wearable-side widget.

1.	Open Tizen IDE for Wearable.

2.	Open the Project Explorer. Open the res folder, then the xml folder. Accessoryservices.xml is the service contract of the connection between the device and the android host. The service Channel id in particular must be correct.


3.	The actual pedometer collection and sending data to the Android host code is under the js folder in main.js. Credit goes to http://denvycom.com/blog/accessing-sensor-data-on-samsung-gear-2/


4.	Right click on ActivizeConsumer in the Project Explorer. Select BuildProject (F10). This will create an ActivizeConsumer.wgt file. Copy ActivizeConsumer.wgt.


5.	Close Tizen IDE for Wearable. Open Eclipse.


6.	Paste the ActivizeConsumer.wgt file in the assets folder under the ActivizeProvider project.


7.	To install the application, go to Settings, then Application manager on the Android device. Click on HelloAccessory (P) to uninstall.


8.	Right click on the ActivizeProvider project in eclipse. Scroll down to Run As-> Android application. Make sure the Android device is connected to the computer via USB. Choose a running Android device and press ok.


9.	From the main menu of the Gear, swipe twice to the right to Apps, and then click Activize. You must hit Connect for the connection between the device and the host to be established. As soon as you start walking, messages will be sent to the phone and up to Service Bus Event Hubs when pedometer values change. Fetch sends a message on command. The wearable app console logs all the messages being sent. Disconnect ends the connection.


For more detailed notes on the host-side Android app:


1.	The host has an analogous accessoryservices.xml file under res/xml. 


2.	The AndroidManifest.xml file must contain these permissions: 
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="com.samsung.accessory.permission.ACCESSORY_FRAMEWORK" />
    <uses-permission android:name="com.samsung.wmanager.APP" />
    <uses-permission android:name="com.samsung.wmanager.ENABLE_NOTIFICATION" /> 
    <uses-permission android:name="com.samsung.android.providers.context.permission.WRITE_USE_APP_FEATURE_SURVEY" /> 
    <uses-permission android:name="com.samsung.WATCH_APP_TYPE.Integrated"/>

        
        <uses-sdk
        android:minSdkVersion="14"
        android:targetSdkVersion="17" />
        
        <uses-permission android:name="android.permission.INTERNET" />

3.	The address string for the send method in the UsingSwig class must be set.
 

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


