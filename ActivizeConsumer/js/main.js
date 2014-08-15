var SAAgent = null;
var SASocket = null;
var CHANNELID = 104;
var ProviderAppName = "ActivizeProvider";

function createHTML(log_string)
{
	var log = document.getElementById('resultBoard');
	log.innerHTML = log.innerHTML + "<br> : " + log_string;
}

function onerror(err) {
	// createHTML("in on error");
	createHTML("err [" + err.name + "] msg[" + err.message + "]");
	console.log("err [" + err.name + "] msg[" + err.message + "]");
}

var agentCallback = {
	onconnect : function(socket) {
		SASocket = socket;
		alert("Activize Connection established with RemotePeer");
		createHTML("startConnection");
		SASocket.setSocketStatusListener(function(reason){
			console.log("Service connection lost, Reason : [" + reason + "]");
			disconnect();
		});
	},
	onerror : onerror
};

var peerAgentFindCallback = {
	onpeeragentfound : function(peerAgent) {
		try {
			// createHTML("in on peer agent found");
			if (peerAgent.appName == ProviderAppName) {
				SAAgent.setServiceConnectionListener(agentCallback);
				SAAgent.requestServiceConnection(peerAgent);
			} else {
				alert("Not expected app!! : " + peerAgent.appName);
			}
		} catch(err) {
			console.log("exception [" + err.name + "] msg[" + err.message + "]");
		}
	},
	onerror : onerror
}

function onsuccess(agents) {
	// createHTML("in on success");
	try {
		if (agents.length > 0) {
			SAAgent = agents[0];
			
			SAAgent.setPeerAgentFindListener(peerAgentFindCallback);
			SAAgent.findPeerAgents();
		} else {
			alert("Not found SAAgent!!");
		}
	} catch(err) {
		console.log("exception [" + err.name + "] msg[" + err.message + "]");
	}
}

function connect() {
	// createHTML("in connect method");
	if (SASocket) {
		alert('Already connected!');
        return false;
    }
	try {
		webapis.sa.requestSAAgent(onsuccess, onerror);
	} catch(err) {
		createHTML("error: " + err.name  + err.message);
		console.log("exception [" + err.name + "] msg[" + err.message + "]");
	}
}

function disconnect() {
	try {
		if (SASocket != null) {
			SASocket.close();
			SASocket = null;
			createHTML("closeConnection");
		}
	} catch(err) {
		console.log("exception [" + err.name + "] msg[" + err.message + "]");
	}
}

function onreceive(channelId, data) {
	createHTML(data);
}

function fetch() {
	try {
		SASocket.setDataReceiveListener(onreceive);
		var stringData = JSON.stringify(pedometerData);
		SASocket.sendData(CHANNELID, stringData);
	} catch(err) {
		console.log("exception [" + err.name + "] msg[" + err.message + "]");
	}
}

var pedometer = null, 
pedometerData = {},
/* lastData = {}, */
CONTEXT_TYPE = 'PEDOMETER';
pedometer = window.webapis.motion;

 //Start pedometer
 function getPedometerData(pedometerInfo) {
     var pData = {
         calorie: pedometerInfo.cumulativeCalorie,
         distance: pedometerInfo.cumulativeDistance,
         runStep: pedometerInfo.cumulativeRunStepCount,
         totalStep: pedometerInfo.cumulativeTotalStepCount,
         walkStep: pedometerInfo.cumulativeWalkStepCount
     };

     pedometerData = pData;
     /* lastData = pedometerData; */
 	
     return pData;
 }

 /**
  * Return last received motion data
  * @return {object}
  */
 function getData() {
     return pedometerData;
 }

 /**
  * Reset pedometer data
  */
 function resetData() {
     pedometerData = {
         calorie: 0,
         distance: 0,
         runStep: 0,
         totalStep: 0,
         walkStep: 0
     };
 }

 /**
  * @param {PedometerInfo} pedometerInfo
  * @param {string} eventName
  */
 function handlePedometerInfo(pedometerInfo, eventName) {
	 pedometerData = getPedometerData(pedometerInfo)
	 console.log('Total Steps : ' + pedometerData.totalStep);
	 document.getElementById("steps").innerHTML =  'Total Steps : ' + pedometerData.totalStep;
	 document.getElementById("calories").innerHTML = 'Calories Burnt : ' + pedometerData.calorie;
	 document.getElementById("distance").innerHTML = 'Distance : ' + pedometerData.distance;
	 document.getElementById("runStep").innherHTML = 'Run Steps : ' + pedometerData.runStep;
	 document.getElementById("walkStep").innerHTML = 'Walk Steps : ' + pedometerData.walkStep;
	 fetch();
 }

 /**
  * Registers a change listener
  * @public
  */
 function start() {
     resetData();
     pedometer.start(
         CONTEXT_TYPE,
         function onSuccess(pedometerInfo) {
             handlePedometerInfo(pedometerInfo, 'pedometer.change');
         }
     );
 }
 
 /**
  * Unregisters a change listener
  * @public
  */
 function stop() {
     pedometer.stop(CONTEXT_TYPE);
 }


window.onload = function () {
    // add eventListener for tizenhwkey
    document.addEventListener('tizenhwkey', function(e) {
        if(e.keyName == "back")
            tizen.application.getCurrentApplication().exit();
    });
    start();
};
