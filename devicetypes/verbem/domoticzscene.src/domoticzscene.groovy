/**
 *  Domoticz Scene SubType Switch.
 *
 *  SmartDevice type for domoticz scenes and groups
 *  
 *
 *  Copyright (c) 2016 Martin Verbeek
 *
 *  Revision History
 *  ----------------
 *	3.2 2019-01-02 More clean implement
 */

metadata {
    definition (name:"domoticzScene", namespace:"verbem", author:"Martin Verbeek", vid:"generic-button") {
        capability "Actuator"
        capability "Sensor"
        capability "Switch"
        capability "Refresh"
		capability "Health Check"
}

    tiles(scale:2) {
    	multiAttributeTile(name:"richDomoticzScene", type:"lighting",  width:6, height:4, canChangeIcon: true, canChangeBackground: true) {
        	tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label:'Off', icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", action:"on", nextState:"On"
                attributeState "Off", label:'Off', icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", action:"on", nextState:"On"
                attributeState "OFF", label:'Off',icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", action:"on", nextState:"On"
                //attributeState "Turning Off", label:'Turning Off', icon:"st.lights.philips.hue-single", backgroundColor:"#FE9A2E", nextState:"Turning On"
                
                attributeState "on", label:'On', icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", action:"off", nextState:"Off"
                attributeState "On", label:'On', icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", action:"off", nextState:"Off"
                attributeState "ON", label:'On', icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", action:"off", nextState:"Off"
                //attributeState "Turning On", label:'Turning On', icon:"st.lights.philips.hue-single", backgroundColor:"#FE9A2E", nextState:"Turning Off"
				attributeState "Error", label:"Install Error", backgroundColor: "#bc2323"
            }
        }
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width:2, height:2) {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main(["richDomoticzScene"])
        
        details(["richDomoticzScene", "refresh"])
    }
}

def refresh() {

    if (parent) {
        parent.domoticz_scenepoll(getIDXAddress())
    }
}

def on() {

    if (parent) {
        parent.domoticz_sceneon(getIDXAddress())
        parent.domoticz_scenepoll(getIDXAddress())
    }
    sendEvent(name:"switch", value: "on")
}

def off() {

    if (parent) {
        parent.domoticz_sceneoff(getIDXAddress())
        parent.domoticz_scenepoll(getIDXAddress())
    }
    sendEvent(name:"switch", value: "off")
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
        sendEvent(name: "switch", value: "Error", descriptionText: "$device.displayName You cannot use this DTH without the related SmartAPP Domoticz Server", isStateChange: true)
    }
}