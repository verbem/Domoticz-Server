/**
 *  domoticzSensor "Air Quality" component
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
	definition (name: "domoticzSensor Air Quality Sensor", namespace: "verbem", author: "Martin Verbeek") {
		capability "Air Quality Sensor"
		capability "Sensor"
        capability "Health Check"
	}

	tiles
    {
		standardTile("sensorAirQuality", "device.airQuality", decoration: "flat", width: 2, height: 2) {
			state "airQuality", label:'${currentValue} ppm', unit:"", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzsensor-air-quality-sensor.src/airQuality.png"
			state "Error", label: "Install Error", backgroundColor: "#bc2323"
		}        
		main (["sensorAirQuality"])
		details(["sensorAirQuality"])
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
        sendEvent(name: "airQuality", value: 0)
    }
    else {
    	log.error "You cannot use this DTH without the related DTH domoticzSensor, the device needs to be a child of this DTH"
        sendEvent(name: "airQuality", value: "Error", descriptionText: "$device.displayName You cannot use this DTH without the related domoticzSensor DTH", isStateChange: true)
    }
  }
  catch (e) {
  	log.error e
  }
}