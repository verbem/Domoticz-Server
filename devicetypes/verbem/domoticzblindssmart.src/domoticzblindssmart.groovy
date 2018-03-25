/**
 *  domoticzBlindsSmart
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
 *  2.1 2016-11-21 Rework of setColor 
 *	2.2 2016-12-01 added calibration of the closing time, now you can use setlevel or ask alexa to dim to a percentage
 *	3.0 2016-12-24 cleanup of DTH statuses
 *  3.1 2017-05-10 Adding end of day scheduling for blinds as an offset to sunset, these are set in the Smart Screens app. 
 *  3.2 2017-07-12 Adding HC and parent check
 */
import groovy.time.TimeCategory 
import groovy.time.TimeDuration


metadata {
	definition (name: "domoticzBlindsSmart", namespace: "verbem", author: "Martin Verbeek") {
    
        capability "Sensor"
		capability "Health Check"

        attribute "windBearing", "string"
        attribute "windSpeed", "number"
		attribute "cloudCover", "number"
		attribute "sunBearing", "string"
    }

    tiles (scale: 2) {

 		standardTile("windBearing", "device.windBearing",  inactiveLabel: false, width: 2, height: 2, decoration:"flat") {
			state "windBearing", label:'${currentValue}', unit:"", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzblinds.src/windBearing.png"            
        }

 		standardTile("windSpeed", "device.windSpeed",  inactiveLabel: false, width: 2, height: 2, decoration:"flat") {
			state "windSpeed", label:'${currentValue} km/h', unit:"km/h", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzblinds.src/windSpeed.png",            
							backgroundColors:[
							// km/h -> bft
							[value: 0, color: "#bef9b8"], 	//bft0 
							[value: 1, color: "#a8f99f"], 	//bft1 
							[value: 6, color: "#90f984"],	//bft2 
							[value: 12, color: "#72fc62"],	//bft3 
							[value: 20, color: "#4efc3a"],	//bft4 
							[value: 29, color: "#1efc05"],	//bft5 
							[value: 39, color: "#f6f7e8"],	//bft6 
							[value: 50, color: "#f1f7a5"],	//bft7
							[value: 62, color: "#fafc74"],	//bft8 
							[value: 75, color: "#f9fc20"],	//bft9 
							[value: 89, color: "#f7ae60"],	//bft10 
							[value: 103, color: "#fc8a11"],	//bft11 
							[value: 117, color: "#f9260e"]	//bft12 
							]        
        }

 		standardTile("sunBearing", "device.sunBearing",  inactiveLabel: false, width: 2, height: 2, decoration:"flat") {
			state "sunBearing", label:'${currentValue}', unit:"", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzblinds.src/sunBearing.png"            
        }

 		standardTile("cloudCover", "device.cloudCover",  inactiveLabel: false, width: 2, height: 2, decoration:"flat") {
			state "cloudCover", label:'${currentValue}%', unit:"%", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzblinds.src/cloudCover.png"            
        }
        
        main(["windBearing"])
        details(["windBearing", "windSpeed", "sunBearing", "cloudCover"])
    }    
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