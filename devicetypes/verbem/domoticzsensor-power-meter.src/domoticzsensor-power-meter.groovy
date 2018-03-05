/**
 *  domoticzSensor "Power" component
 *
 *  Copyright 2018 Martin Verbeek
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
	definition (name: "domoticzSensor Power Meter", namespace: "verbem", author: "Martin Verbeek") {
		capability "Sensor"       
    }

	tiles
    {
		standardTile("sensorPower", "device.power", decoration: "flat", width: 2, height: 2) {
			state "power", label: '${currentValue} W', icon: "http://cdn.device-icons.smartthings.com/Electronics/electronics6-icn@2x.png"
			state "Error", label: "Install Error", backgroundColor: "#bc2323"
		}        
		main (["sensorPower"])
		details(["sensorPower"])
	}
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
        sendEvent(name: "power", value: 0)
    }
    else {
    	log.error "You cannot use this DTH without the related DTH domoticzSensor, the device needs to be a child of this DTH"
        sendEvent(name: "power", value: "Error", descriptionText: "$device.displayName You cannot use this DTH without the related domoticzSensor DTH", isStateChange: true)
    }
  }
  catch (e) {
  	log.error e
  }
}