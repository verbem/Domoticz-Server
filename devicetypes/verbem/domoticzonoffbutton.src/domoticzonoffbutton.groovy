/**
 *  domoticzSelector "Button" component
 *
 *  Copyright 2017 Martin Verbeek
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
	definition (name: "domoticzOnOffButton", namespace: "verbem", author: "Martin Verbeek") {
		capability "Button"
		capability "Actuator"
		capability "Sensor"
        
        command "buttonPress"
        attribute "labelButton", "string"
	}

	tiles
    {
		standardTile("stateButton", "device.labelButton", decoration: "flat", width: 2, height: 1) {
			state "labelButton", label: '${currentValue}', action: "buttonPress"
			state "Error", label: "Install Error", backgroundColor: "#bc2323"
		}        
		main (["stateButton"])
		details(["stateButton"])
	}
}

def on() {
	buttonPress()
}

def buttonPress() {
	sendEvent(name:"button", value: "pushed", data: [buttonNumber: 1], descriptionText: "$device.displayName button ${device.displayName.split("=")[1]} was pushed", isStateChange: true)
	parent.callMood(device.displayName.split("=")[1])
}

def installed() {
	initialize()
}

def updated() {
	initialize()
}

def initialize() {

    try {
    if (parent) {
        sendEvent(name: "labelButton", value: device.displayName.split("=")[1])
    }
    else {
    	log.error "You cannot use this DTH without the related DTH domoticzOnOff, the device needs to be a child of this DTH"
        sendEvent(name: "button", value: "Error", descriptionText: "$device.displayName You cannot use this DTH without the related SmartAPP Domoticz Server", isStateChange: true)
    }
  }
  catch (e) {
  	log.error e
  }
}