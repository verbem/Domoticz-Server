/**
 *  domoticzSensor "Sound" component
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
	definition (name: "domoticzSensor Sound Sensor", namespace: "verbem", author: "Martin Verbeek") {
		capability "Sound Pressure Level"
		capability "Sensor"
		capability "Sound Sensor"
	}

	tiles
    {
		standardTile("sensorSound", "device.soundPressureLevel", decoration: "flat", width: 2, height: 2) {
			state "soundPressureLevel", label: '${currentValue} dB', icon: "http://cdn.device-icons.smartthings.com/Entertainment/entertainment3-icn@2x.png"
			state "Error", label: "Install Error", backgroundColor: "#bc2323"
		}        
		main (["sensorSound"])
		details(["sensorSound"])
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
        sendEvent(name: "soundPressureLevel", value: 0)
    }
    else {
    	log.error "You cannot use this DTH without the related DTH domoticzSensor, the device needs to be a child of this DTH"
        sendEvent(name: "soundPressureLevel", value: "Error", descriptionText: "$device.displayName You cannot use this DTH without the related domoticzSensor DTH", isStateChange: true)
    }
  }
  catch (e) {
  	log.error e
  }
}