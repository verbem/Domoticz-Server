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
        capability "Image Capture"

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
        
        carouselTile("graph", "device.image", width: 6, height: 4)
        
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
            
        main(["graph"])
        
        details(["day", "week", "month", "year", "graph"])
    }
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

def take(Map parentState) {
	log.debug "Take() child"
    log.debug parentState
	def imageCharts = "https://image-charts.com/chart?"
	def params = [uri: "${imageCharts}chs=720x480&chd=${parentState.chd}&cht=bvg&chds=a&chxt=x,y&chxl=${parentState.chxl}&chts=0000FF,20&chco=${parentState.chco}&chtt=${parentState.chtt}"]
    
    if (state.imgCount == null) state.imgCount = 0
 
    try {
        httpGet(params) { response ->
        	
            if (response.status == 200 && response.headers.'Content-Type'.contains("image/png")) {
                def imageBytes = response.data
                if (imageBytes) {
                    state.imgCount = state.imgCount + 1
                    def name = "PowerUsage$state.imgCount"

                    // the response data is already a ByteArrayInputStream, no need to convert
                    try {
                        storeImage(name, imageBytes, "image/png")
                    } catch (e) {
                        log.error "error storing image: $e"
                    }
                }
            }
        else log.error "wrong format of content"
        }
    } catch (err) {
        log.error ("Error making request: $err")
    }
}

private def getResponse(evt) {

    if (evt instanceof physicalgraph.device.HubResponse) {
        return evt.json
    }
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
    	log.error "You cannot use this DTH without the related SmartAPP Domoticz Server, the device needs to be a child of this App"
    }
}