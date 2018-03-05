/**
 *  Domoticz Dusk SensorSubType Switch.
 *
 *  SmartDevice type for domoticz dusk sensor switch
 *  
 *
 *  Copyright (c) 2017 Martin Verbeek
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
 *  2017-08-12 3.13 Initial
 */

metadata {
    definition (name:"domoticzDuskSensor", namespace:"verbem", author:"Martin Verbeek") {
        capability "Actuator"
        capability "Sensor"
        capability "Switch"
        capability "Polling"
        capability "Signal Strength"
		capability "Health Check"
    }

    tiles(scale:2) {
    	multiAttributeTile(name:"richDomoticzDusk", type:"lighting",  width:6, height:4, canChangeIcon: true, canChangeBackground: true) {
        	tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'Dusk', icon:"st.Weather.weather4", backgroundColor:"#808080"
                attributeState "On", label:'Dusk', icon:"st.Weather.weather4", backgroundColor:"#808080"
                attributeState "ON", label:'Dusk', icon:"st.Weather.weather4", backgroundColor:"#808080"
                
                attributeState "off", label:'Day', icon:"st.Weather.weather14", backgroundColor:"#ffcc00"
                attributeState "Off", label:'Day', icon:"st.Weather.weather14", backgroundColor:"#ffcc00"
                attributeState "OFF", label:'Day', icon:"st.Weather.weather14", backgroundColor:"#ffcc00"               
            }
        }
     
	standardTile("rssi", "device.rssi", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "rssi", label:'Signal ${currentValue}', unit:"", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzsensor.src/network-signal.png"
		}

        main(["richDomoticzDusk"])
        
        details(["richDomoticzDusk", "rssi"])
    }
}

// switch.poll() command handler
def poll() {

    if (parent) {
        TRACE("poll() ${device.deviceNetworkId}")
        parent.domoticz_poll(getIDXAddress())
    }
}

// switch.poll() command handler
def refresh() {

    if (parent) {
        parent.domoticz_poll(getIDXAddress())
    }
}

// switch.on() command handler
def on() {

    if (parent) {
        parent.domoticz_on(getIDXAddress())
    }
}

// switch.off() command handler
def off() {

    if (parent) {
        parent.domoticz_off(getIDXAddress())
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