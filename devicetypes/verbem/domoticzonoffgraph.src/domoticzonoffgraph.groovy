/**
 *  Domoticz OnOff SubType Graph.
 *
 *  SmartDevice type for domoticz switches and dimmers.
 *  
 *
 *  Copyright (c) 2018 Martin Verbeek
 *
 *
 *  Revision History
 *  ----------------
 	V7.00 Intitial Graph Component

 */
import Calendar.*
import groovy.time.*

metadata {
    definition (name:"domoticzOnOffGraph", namespace:"verbem", author:"Martin Verbeek") {
    	capability "Configuration"
        capability "Actuator"
        capability "Sensor"

        // custom commands
        command "hourLog"
        command	"day"
        command "week"
        command "month"
        command "year"
        
        attribute "hour", "string"
        attribute "graph", "string"
    }

    tiles(scale:2) {
    
    	htmlTile(name:"webGraph",
                action: "getGraphHTML",
                refreshInterval: 1,
                width: 6,
                height: 4, 
                whitelist: ["www.gstatic.com"])
        
        
        standardTile("HourLog", "device.hour", decoration: "flat", width: 1, height: 1) {
        	state "Graph", label:'48 Hour Log', action: "hourLog", defaultState: true
        	state "noGraph", label:'No 48 Hour Log', action: "hourLog"
        }
     
        standardTile("day", "device.graph", decoration: "flat", width: 1, height: 1) {
        	state "Graph", label:'Day Usage', action: "day", defaultState: true
        	state "noGraph", label:'No Day Usage', action: "day"
        }
     
        standardTile("week", "device.graph", decoration: "flat", width: 1, height: 1) {
        	state "Graph", label:'Week Usage', action: "week", defaultState: true
        	state "noGraph", label:'No Week Usage', action: "week"
        }
     
        standardTile("month", "device.graph", decoration: "flat", width: 1, height: 1) {
        	state "Graph", label:'Month Usage', action: "month", defaultState: true
        	state "noGraph", label:'No Month Usage', action: "month"
        }
     
        standardTile("year", "device.graph", decoration: "flat", width: 1, height: 1) {
        	state "Graph", label:'Year Usage', action: "year", defaultState: true
        	state "noGraph", label:'No Year Usage', action: "year"
        }
            
        main(["webGraph"])
        
        details(["day", "week", "month", "year", "webGraph"])
    }
}

mappings {
 	path("/getGraphHTML") {action: [GET: "getGraphHTML"]}
}

def parse(Map message) {
	log.info message
    
    if (message.name == "button") {
    	sendEvent(name: "switch", value: message.value)
    	def children = getChildDevices()
        children.each { child ->
        	if (child.displayName.split("=")[1] == message.value) child.sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], descriptionText: "$device.displayName button $message.value was pushed", isStateChange: true)   	
    	}
    }
    return createEvent(message)   
}

private def TRACE(message) {
    log.debug message
}

private def STATE() {
    log.debug "switch is ${device.currentValue("switch")}"
    log.debug "deviceNetworkId: ${device.deviceNetworkId}"
}

// gets the IDX address of the device
private getIDXAddress() {
    
    def idx = parent.getDataValue("idx")
        
    if (!idx) {
        def parts = parent.device.deviceNetworkId.split(":")
        if (parts.length == 3) {
            idx = parts[2]
        } else {
            log.warn "Can't figure out idx for device: ${device.name} returning ${device.deviceNetworkId}"
            return device.deviceNetworkId.toString()
        }
    }

    //log.debug "Using IDX: $idx for device: ${device.id}"
    return idx
}

// Graph related commands

def hourLog() {
    if (parent.parent.name == "Domoticz Server") {
    	sendEvent(name:"hour", value:"Graph")
    	sendLightlogRequest()    	
    }
    else sendEvent(name:"hour", value:"noGraph")
}

def day() {
	parent.day()
}

def week() {
	parent.week()
}

def month() {
	parent.month()
}

def year() {
	parent.year()
}

def getDataString(Integer seriesIndex) {
 	def dataString = "2,3,5,7,10"
 	return dataString
}

def getStartTime() {
	def startTime = 24
	
	return startTime
}

def installed() {
	initialize()
}

def updated() {
	initialize()
}

def initialize() {

	if (parent) {
        sendEvent(name: "DeviceWatch-Enroll", value: groovy.json.JsonOutput.toJson([protocol: "LAN", scheme:"untracked"]), displayed: false)
    }
    else {
    	log.error "You cannot use this DTH without the domoticzOnOff DTH, the device needs to be a child of this DTH"
    }
}

def getGraphHTML() {
	def html = """
		<!DOCTYPE html>
			<html>
				<head>
					<meta http-equiv="cache-control" content="max-age=0"/>
					<meta http-equiv="cache-control" content="no-cache"/>
					<meta http-equiv="expires" content="0"/>
					<meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT"/>
					<meta http-equiv="pragma" content="no-cache"/>
					<meta name="viewport" content="width = device-width">
					<meta name="viewport" content="initial-scale = 1.0, user-scalable=no">
					<style type="text/css">body,div {margin:0;padding:0}</style>
					<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
					<script type="text/javascript">
						google.charts.load('current', {packages: ['corechart']});
						google.charts.setOnLoadCallback(drawGraph);
						function drawGraph() {
							var data = new google.visualization.DataTable();
							data.addColumn('timeofday', 'time');
							data.addColumn('number', 'Energy (Yesterday)');
							data.addColumn('number', 'Power (Yesterday)');
							data.addColumn('number', 'Energy (Today)');
							data.addColumn('number', 'Power (Today)');
							data.addRows([
								"2,3,5,7,10"
							]);
							var options = {
								fontName: 'San Francisco, Roboto, Arial',
								height: 240,
								hAxis: {
									format: 'H:mm',
									minValue: [${getStartTime()},0,0],
									slantedText: false
								},
								series: {
									0: {targetAxisIndex: 1, color: '#FFC2C2', lineWidth: 1},
									1: {targetAxisIndex: 0, color: '#D1DFFF', lineWidth: 1},
									2: {targetAxisIndex: 1, color: '#FF0000'},
									3: {targetAxisIndex: 0, color: '#004CFF'}
								},
								vAxes: {
									0: {
										title: 'Power (W)',
										format: 'decimal',
										textStyle: {color: '#004CFF'},
										titleTextStyle: {color: '#004CFF'},
										viewWindow: {min: 0}
									},
									1: {
										title: 'Energy (kWh)',
										format: 'decimal',
										textStyle: {color: '#FF0000'},
										titleTextStyle: {color: '#FF0000'},
										viewWindow: {min: 0},
                                        gridlines: {count: 0}
									}
								},
								legend: {
									position: 'none'
								},
								chartArea: {
									width: '72%',
									height: '85%'
								}
							};
							var chart = new google.visualization.AreaChart(document.getElementById('chart_div'));
							chart.draw(data, options);
						}
					</script>
				</head>
				<body>
					<div id="chart_div"></div>
				</body>
			</html>
		"""
	render contentType: "text/html", data: html, status: 200
}