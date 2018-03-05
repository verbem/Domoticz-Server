/**
 *  domoticzSmokeDetector
 *
 *  Copyright 2016 Martin Verbeek
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "domoticzSmokeDetector", namespace: "verbem", author: "Martin Verbeek") {
		capability "Smoke Detector"
		capability "Temperature Measurement"
		capability "Actuator"
		capability "Refresh"
		capability "Battery"
        capability "Signal Strength"
		capability "Health Check"
}

    tiles (scale: 2){
        multiAttributeTile(name:"smoke", type: "lighting", width: 6, height: 4){
            tileAttribute ("device.smoke", key: "PRIMARY_CONTROL") {
                attributeState("clear", label:"CLEAR", icon:"st.alarm.smoke.clear", backgroundColor:"#ffffff")
                attributeState("off", label:"CLEAR", icon:"st.alarm.smoke.clear", backgroundColor:"#ffffff")
                attributeState("Off", label:"CLEAR", icon:"st.alarm.smoke.clear", backgroundColor:"#ffffff")
                attributeState("OFF", label:"CLEAR", icon:"st.alarm.smoke.clear", backgroundColor:"#ffffff")
                attributeState("smoke", label:"SMOKE", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
                attributeState("on", label:"SMOKE", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
                attributeState("On", label:"SMOKE", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
                attributeState("ON", label:"SMOKE", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
                attributeState("tested", label:"TEST", icon:"st.alarm.smoke.test", backgroundColor:"#e86d13")
				attributeState("Error", label:"Install Error", backgroundColor: "#bc2323")
            }
            tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
                attributeState "battery", label:'Battery: ${currentValue}%', unit:"%"
            }
        }
        standardTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "battery", label:'${currentValue}% battery', unit:"%", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzsensor.src/battery.png"
        }

		standardTile("temperature", "device.temperature", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "temperature", label:'${currentValue}°', unit:"C", icon:"st.Weather.weather2"
        }

		standardTile("rssi", "device.rssi", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "rssi", label:' Signal ${currentValue}', unit:"", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzsensor.src/network-signal.png"
        }

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        main "smoke"
        details(["smoke","temperature", "battery", "rssi", "refresh"])
	}
}

def refresh() {
	log.debug "Executing 'refresh'"

    if (parent) {
        parent.domoticz_poll(getIDXAddress())
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
        sendEvent(name: "contact", value: "Error", descriptionText: "$device.displayName You cannot use this DTH without the related SmartAPP Domoticz Server", isStateChange: true)
    }
}