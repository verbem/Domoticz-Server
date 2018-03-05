/**
 *  domoticzSensor "Barometric Pressure" component
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
	definition (name: "domoticzSensor Barometric Pressure", namespace: "verbem", author: "Martin Verbeek") {
		capability "Sensor"
		attribute "barometricPressure", "number"
	}

	tiles
    {
		standardTile("sensorBarometricPressure", "device.barometricPressure", decoration: "flat", width: 2, height: 2) {
			state "sensorBarometricPressure", label: '${currentValue} hPa', icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzsensor.src/barometer-icon-png-5.png"
			state "Error", label: "Install Error", backgroundColor: "#bc2323"
		}        
		main (["sensorBarometricPressure"])
		details(["sensorBarometricPressure"])
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
        sendEvent(name: "barometricPressure", value: 0)
    }
    else {
    	log.error "You cannot use this DTH without the related DTH domoticzSensor, the device needs to be a child of this DTH"
        sendEvent(name: "barometricPressure", value: "Error", descriptionText: "$device.displayName You cannot use this DTH without the related domoticzSensor DTH", isStateChange: true)
    }
  }
  catch (e) {
  	log.error e
  }
}