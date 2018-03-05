/**
 *  Domoticz Scene SubType Switch.
 *
 *  SmartDevice type for domoticz scenes and groups
 *  
 *
 *  Copyright (c) 2016 Martin Verbeek
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  Revision History
 *  ----------------
 *  2.1 2016-11-21 Rework of setColor 
 *	2.2 2016-12-01 added calibration of the closing time, now you can use setlevel or ask alexa to dim to a percentage
 *	3.0 2016-12-24 cleanup of DTH statuses
 *	3.1 2016-12-24 Implement PUSH as the button for a Scene
 */

metadata {
    definition (name:"domoticzScene", namespace:"verbem", author:"Martin Verbeek") {
        capability "Actuator"
        capability "Sensor"
        capability "Switch"
        capability "Refresh"
        capability "Polling"
		capability "Health Check"
}

    tiles(scale:2) {
    	multiAttributeTile(name:"richDomoticzOnOff", type:"lighting",  width:6, height:4, canChangeIcon: true, canChangeBackground: true) {
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
        
        standardTile("debug", "device.motion", inactiveLabel: false, decoration: "flat", width:2, height:2) {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main(["richDomoticzOnOff"])
        
        details(["richDomoticzOnOff", "debug"])

        simulator {
            // status messages
            status "Switch On": "switch:1"
            status "Switch Off": "switch:0"
        }
    }
}

// switch.poll() command handler
def poll() {

    if (parent) {
        parent.domoticz_scenepoll(getIDXAddress())
    }
}

// switch.poll() command handler
def refresh() {

    if (parent) {
        parent.domoticz_scenepoll(getIDXAddress())
    }
}

// switch.on() command handler
def on() {

    if (parent) {
        parent.domoticz_sceneon(getIDXAddress())
        parent.domoticz_scenepoll(getIDXAddress())
    }
}

// switch.off() command handler
def off() {

    if (parent) {
        parent.domoticz_sceneoff(getIDXAddress())
        parent.domoticz_scenepoll(getIDXAddress())
    }
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