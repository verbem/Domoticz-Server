/**
 *  domoticzPowerReport
 *
 *  Copyright 2019 Martin Verbeek
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
 * 7.29 consumptionLow from DZ Server and integers
 *
 */  
metadata {
	definition (name: "domoticzPowerReport", namespace: "verbem", author: "Martin Verbeek") {
		capability "Sensor"
		capability "Actuator"
        capability "Battery"
        capability "Signal Strength"
		capability "Health Check"
		capability "Power Meter"
        capability "Power Consumption Report"
        capability "Energy Meter"
        capability "Image Capture"
        }
        
	tiles(scale: 2) {       
        multiAttributeTile(name:"powerReport", type:"generic", width:6, height:4) {
            tileAttribute("device.power", key: "PRIMARY_CONTROL") {
                attributeState "level", label:'${currentValue}W', unit: "W" , defaultState: true, action: "take", backgroundColors:[                   
                    [value: 0, color: "#153591"],      	//dark blue
                    [value: 1000, color: "#1e9cbb"],    //light blue
                    [value: 2200, color: "#90d2a7"],	//greenish
                    [value: 3300, color: "#44b621"],	//dark green
                    [value: 5000, color: "#f1d801"],	//dark yellow
                    [value: 7500, color: "#d04e00"],	//dark orange
                    [value: 10000, color: "#bc2323"]	//red
                ]
            }
            tileAttribute("device.energyMeter", key: "SECONDARY_CONTROL") {
                attributeState "energyMeter", label:'${currentValue}kWh', unit: "kWh"
            }
        }
        
        valueTile("consumptionHigh", "device.consumptionHigh", width: 2, height: 2) {
        	state "consumptionHigh", label:'High ${currentValue}kWh', defaultState: true
    	}
        valueTile("consumptionLow", "device.consumptionLow", width: 2, height: 2) {
        	state "consumptionLow", label:'Low ${currentValue}kWh', defaultState: true
    	}
        valueTile("momentaryUsage", "device.momentaryUsage", width: 2, height: 2) {
        	state "momentaryUsage", label:'Usage ${currentValue}W', defaultState: true
    	}

	}
	main "powerReport"
    details(["powerReport","consumptionHigh","consumptionLow"])
}

// parse events into attributes
def parse(String description) {
	log.error "Parsing '${description}'"
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
        sendEvent(name: "powerTotal", value: "Error", descriptionText: "$device.displayName You cannot use this DTH without the related SmartAPP Domoticz Server", isStateChange: true)
    }
}