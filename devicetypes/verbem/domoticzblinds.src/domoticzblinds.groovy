
/**
 *  domoticzBlinds
 *
 *  Copyright 2018 Martin Verbeek
 *
 *	4.0 2018-02-12 Add windowShade capability, fix eodDone
 *	4.1	2018-04-05 Introduce configure for all non standard attributes and commands
 *	4.2	2018-06-02 Moved EOD processing to SM
 *	4.3	2018-06-21 Removed calibrate, moved it to timed session capability
 */
import groovy.time.TimeCategory 
import groovy.time.TimeDuration

preferences {
    input(name:"stopSupported", type:"bool", title: "Stop command supported?", description:"Does your blind use the STOP command to halt the blind. NOT to be confused with the Somfy Stop/My command!", defaultValue:false)
}   
metadata {
	definition (name: "domoticzBlinds", namespace: "verbem", author: "Martin Verbeek", vid: "generic-shade", ocfdevicetype: "oic.d.blind") {
    
        capability "Actuator"
        capability "Switch"
        capability "Sensor"
        capability "Switch Level"
        capability "Refresh"
        capability "Signal Strength"
		capability "Health Check"
        capability "Window Shade"
        capability "Configuration"
        capability "Timed Session"

    }

    tiles (scale: 2) {
	    multiAttributeTile(name:"richDomoticzBlind", type:"generic",  width:6, height:4, canChangeIcon: true, canChangeBackground: true) {
        	tileAttribute("device.windowShade", key: "PRIMARY_CONTROL") {
                attributeState "open", 				label:"Open", 			backgroundColor:"#e86d13", nextState:"closing", action:"close"
                attributeState "opening", 			label:"Opening", 		backgroundColor:"#e86d13", nextState:""
				attributeState "partially open", 	label:"Partly Open", 	backgroundColor:"#11A81C", action:"open"               
                attributeState "closed", 			label:"Closed",  		backgroundColor:"#00a0dc", nextState:"opening", action:"open"
                attributeState "closing", 			label:"Closing",  		backgroundColor:"#00a0dc", nextState:""
				attributeState "Error", 			label:"Install Error", 	backgroundColor:"#bc2323"
            }
            tileAttribute("device.level", key: "SLIDER_CONTROL", range:"0..16") {
            	attributeState "level", action:"setLevel" 
            }
        }
        
        standardTile("Up", "device.switch", width: 2, height: 2, inactiveLabel:false, decoration:"flat") {
            state "default", label:'Up', icon:"st.doors.garage.garage-opening",
                action:"open"
        }

        standardTile("Preset", "device.switch", width: 2, height: 2, inactiveLabel:false, decoration:"flat") {
            state "default", label:'Preset', icon:"st.doors.garage.garage-open",
                action:"presetPosition"
        }

        standardTile("Down", "device.switch", width: 2, height: 2, inactiveLabel:false, decoration:"flat") {
            state "default", label:'Down', icon:"st.doors.garage.garage-closing",
                action:"close"
        }

		standardTile("Cal", "device.sessionStatus", width: 2, height: 2, inactiveLabel:false, decoration:"flat") {
            state "canceled", label:'Start Calibrate', icon:"st.doors.garage.garage-closing",action:"start"
            state "stopped",  label:'Start Calibrate', icon:"st.doors.garage.garage-closing",action:"start"
            state "running",  label:'Stop Calibrate', icon:"st.doors.garage.garage-closed",action:"stop"
            state "paused", label:'Start Calibrate', icon:"st.doors.garage.garage-closing",action:"start"
            state "default", label:'Start Calibrate', icon:"st.doors.garage.garage-closing",action:"start"
        }

        standardTile("Refresh", "device.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "refresh", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
		standardTile("rssi", "device.rssi", inactiveLabel: false, width: 2, height: 2, decoration:"flat") {
			state "rssi", label:'Signal ${currentValue}', unit:"", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzsensor.src/network-signal.png"
		}
              
		childDeviceTile("windBearing", "SmartScreens", decoration: "flat", width: 2, height: 2, childTileName: "windBearing")   
        childDeviceTile("windSpeed", "SmartScreens", decoration: "flat", width: 2, height: 2, childTileName: "windSpeed")
        childDeviceTile("sunBearing", "SmartScreens", decoration: "flat", width: 2, height: 2, childTileName: "sunBearing")
        childDeviceTile("cloudCover", "SmartScreens", decoration: "flat", width: 2, height: 2, childTileName: "cloudCover")
        
        main(["richDomoticzBlind"])
	    details(["richDomoticzBlind", "Up", "Preset", "Down", "Cal", "rssi", "Refresh", "windBearing", "windSpeed", "sunBearing", "cloudCover"])

    }    
}

// parse events into attributes
def parse(Map message) {
    log.debug "parse(${message})"

    if (message?.size() == 0) {
        log.error "Parse- Invalid message: ${message}"
        return null
    }

	if (message?.name.matches("sunBearing|windBearing|windSpeed|cloudCover")) {
    	childEvent(message)
        return null
    }

    def evt = createEvent(message)
	return evt
}

// handle commands, 
def configure(command) {
	if (command?.setState) state."${command.setState.name}" = command.setState.value
    if (command?.getState) parent.state."${command.getState.name}" = state?."${command.getState.name}"
}

def on() {
	log.debug "on()"
    if (parent) {
		sendEvent(name:'windowShade', value:"closed" as String)
		parent.domoticz_on(getIDXAddress())
    }
}

def off() {
	log.debug "off()"
    if (parent) {
		sendEvent(name:'windowShade', value:"open" as String)
        parent.domoticz_off(getIDXAddress())
    }
}

def close() {
	log.debug "close()"
    if (parent) {
		sendEvent(name:'windowShade', value:"closed" as String)
        parent.domoticz_on(getIDXAddress())
    }
}

def refresh() {
	log.debug "refresh()"

    if (parent) {
        parent.domoticz_poll(getIDXAddress())
    }
    
    if (state.blindClosingTime) {
    	Date compTime = new Date()
        compTime.setTime(state.blindClosingTime)
        sendEvent(name: "completionTime", value: compTime)
    }
}

def open() {
	log.debug "open()"
    if (parent) {
		sendEvent(name:'windowShade', value:"open" as String)
        parent.domoticz_off(getIDXAddress())
    }
}

def presetPosition() {
	log.debug "presetPosition()"
    if (parent) {
        sendEvent(name:'windowShade', value:"partially open" as String)
        parent.domoticz_stop(getIDXAddress())
    }

}

def setLevel(level) {
	log.debug "setLevel() level ${level}"
	state.level = level   
    if (parent) {
    	if (state?.blindClosingTime) {
        	if (state.blindClosingTime > 0 && state.blindClosingTime < 100000) {
        		parent.domoticz_off(getIDXAddress())
				def Sec = Math.round(state.blindClosingTime.toInteger()/1000)
				runIn(Sec, setLevelCloseAgain)
                sendEvent(name:'windowShade', value:"opening" as String)
				log.debug "setLevel() ON in ${Sec} s"         		
            }
        }
        else {
            sendEvent(name:'switch', value:"Stopped" as String)
            sendEvent(name:'windowShade', value:"partially open" as String)
            parent.domoticz_stop(getIDXAddress())
        }
    }
}

def setLevelCloseAgain() {
    parent.domoticz_on(getIDXAddress())
    def Sec = Math.round(state.blindClosingTime.toInteger()/1000)
	Sec = Math.round(Sec*state.level.toInteger()/100) - 1
    log.debug "setLevel() Stop in ${Sec} s"
    runIn(Sec, setLevelStopAgain)
    sendEvent(name:'windowShade', value:"closing" as String)

}

def setLevelStopAgain() {

    if (settings.stopSupported) {
        parent.domoticz_stop(getIDXAddress())
        log.debug "setLevel() STOP"
    	}
    else {
        parent.domoticz_on(getIDXAddress())
        log.debug "setLevel() second ON"
    	}
    sendEvent(name:'switch', value:"Stopped" as String)
    sendEvent(name:'windowShade', value:"partially open" as String)
}
// Timed Session Capability abused for calibrated Blinds
def cancel() {
}
def pause() {
}

def start() {
		log.debug "Timed Session Start()"       
        state.calibrationInProgress = "yes"
        def sTime = new Date().time
        state.startCalibrationTime = sTime
        sendEvent(name: "sessionStatus", value: "running")
        parent.domoticz_on(getIDXAddress())
}

def stop() {
		log.debug "Timed Session Stop()"       
        state.calibrationInProgress = "no"
        def eTime = new Date().time
        state.endCalibrationTime = eTime
        def eT = state.endCalibrationTime
        def sT = state.startCalibrationTime
        def blindClosingTime = (eT - sT)
        state.blindClosingTime = blindClosingTime
        Date compTime = new Date()
        compTime.setTime(state.blindClosingTime)
        sendEvent(name: "completionTime", value: compTime)
        sendEvent(name: "sessionStatus", value: "stopped")
        log.debug "Timed Session Stop() - completionTime ${state.blindClosingTime} ms"       
        parent.domoticz_off(getIDXAddress())
}
def setCompletionTime(completionTime) {
}

private def createComponent() {
	// this will be first call Smart Screens does to the device. If no component child exists, create it now.
    log.info "CreateComponents"
    def children = getChildDevices()
    if (!children) {
    	try {
        	addChildDevice("verbem", "domoticzBlindsSmart", "${device.deviceNetworkId}-SmartScreen", null,
                            [completedSetup: true, label: "${device.displayName} (SmartScreens)",
                             isComponent: true, componentName: "SmartScreens", componentLabel: "SmartScreens"])
            }
        catch (e) {log.error e}
    }
}

// gets the IDX address of the device
private getIDXAddress() {
	
    def idx = getDataValue("idx")
        
    if (!idx) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 3) {
            idx = parts[2]
        } else {
            log.warn "Can't figure out idx for device: ${device.id}"
        }
    }

    //log.debug "Using IDX: $idx for device: ${device.id}"
    return idx
}

/*----------------------------------------------------*/
/*			execute event can be called from the service manager!!!
/*----------------------------------------------------*/
private def childEvent(Map message) {
    def children = getChildDevices()
    def childSmart
    def icon
   
	if (message?.value == null) return   
    if (children) {
 		children.each {
        	childSmart = it
        }
    }
    else {
    	log.error "no children cannot issue sendEvent"
        return
    }
    
    if (message.name == "cloudCover") {      
        switch (message.value.toInteger()) {
            case 0..20:
            icon = "http://icons.wxug.com/i/c/k/clear.gif"
            break
            case 21..50:
            icon = "http://icons.wxug.com/i/c/k/partlycloudy.gif"
            break
            case 51..80:
            icon = "http://icons.wxug.com/i/c/k/mostlycloudy.gif"
            break
            default:
                icon = "http://icons.wxug.com/i/c/k/cloudy.gif"
            break
        } 
    }
    
    if(message.name.matches("windBearing|sunBearing")) {
        icon = "https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzblinds.src/WindDir${message.value}.PNG"
    }    
    
    if (icon) childSmart.sendEvent(name:"${message.name}", value:"${message.value}", data:[icon:icon])
    else childSmart.sendEvent(name:"${message.name}", value:"${message.value}")
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
        createComponent()
    }
    else {
    	log.error "You cannot use this DTH without the related SmartAPP Domoticz Server, the device needs to be a child of this App"
        sendEvent(name: "switch", value: "Error", descriptionText: "$device.displayName You cannot use this DTH without the related SmartAPP Domoticz Server", isStateChange: true)
    }
}