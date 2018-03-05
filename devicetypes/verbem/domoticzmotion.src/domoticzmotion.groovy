/**
 *  domoticzMotion
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
	definition (name: "domoticzMotion", namespace: "verbem", author: "SmartThings") {
    	capability "Configuration"
		capability "Motion Sensor"
		capability "Sensor"
		capability "Actuator"
		capability "Refresh"
		capability "Health Check"
        capability "Power"
        
        attribute "powerToday", "string"
        }

	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00a0dc"
				attributeState "on", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00a0dc"
				attributeState "On", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00a0dc"
				attributeState "ON", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00a0dc"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
				attributeState "off", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
				attributeState "Off", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
				attributeState "OFF", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
				attributeState "Error", label:"Install Error", backgroundColor: "#bc2323"
			}
            tileAttribute("device.powerToday", key: "SECONDARY_CONTROL") {
        		attributeState "powerToday",label:'${currentValue}', icon:"st.switches.switch.on", defaultState: true
            }
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		} 
                    
		childDeviceTile("sensorHumidity", "Relative Humidity Measurement", decoration: "flat", width: 2, height: 2, childTileName: "sensorHumidity")   
		childDeviceTile("sensorBarometricPressure", "Barometric Pressure", decoration: "flat", width: 2, height: 2, childTileName: "sensorBarometricPressure")   
		childDeviceTile("sensorPower", "Power Meter", decoration: "flat", width: 2, height: 2, childTileName: "sensorPower")   
		childDeviceTile("sensorIlluminance", "Illuminance Measurement", decoration: "flat", width: 2, height: 2, childTileName: "sensorIlluminance")   
		childDeviceTile("sensorAirQuality", "Air Quality Sensor", decoration: "flat", width: 2, height: 2, childTileName: "sensorAirQuality")   
		childDeviceTile("sensorSound", "Sound Sensor", decoration: "flat", width: 2, height: 2, childTileName: "sensorSound")   
		childDeviceTile("sensorTemperature", "Temperature Measurement", decoration: "flat", width: 2, height: 2, childTileName: "sensorTemperature")   
		childDeviceTile("sensorSignalStrength", "Signal Strength", decoration: "flat", width: 2, height: 2, childTileName: "sensorSignalStrength")   
		childDeviceTile("sensorBattery", "Battery", decoration: "flat", width: 2, height: 2, childTileName: "sensorBattery")   

		main "motion"
		details(["motion", "sensorTemperature", "sensorBattery", "sensorIlluminance", "sensorSignalStrength", "refresh"])
	}
}

def refresh() {
	log.debug "Executing 'refresh'"

    if (parent) {
        parent.domoticz_poll(getIDXAddress())
        if (device.currentValue("powerToday") == null) sendEvent(name:"powerToday", value:"Usage not reported")
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

def parse(Map message) {
	
    def capability
    def evt = createEvent(message)
    if (message.name.matches("illuminance|soundPressureLevel|temperature|battery|rssi")) {

    	switch (message.name) {
        	case "airQuality":
            capability = "Air Quality Sensor"
            break
        	case "illuminance":
            capability = "Illuminance Measurement"
            break
        	case "temperature":
            capability = "Temperature Measurement"
            break
        	case "soundPressureLevel":
            capability = "Sound Sensor"
            break
        	case "barometricPressure":
            capability = "Barometric Pressure"
            break
        	case "humidity":
            capability = "Relative Humidity Measurement"
            break
        	case "power":
            capability = "Power Meter"
            break
        	case "battery":
            capability = "Battery"
            break
        	case "rssi":
            capability = "Signal Strength"
            break
        }
        if (capability) {
            getChildDevices().each { child ->
                if (child.deviceNetworkId.split("-")[1] == capability) { 
                	log.info "Component Capability : ${capability} Message : ${message}"
                	child.sendEvent(message)
                }
            }
        }
        else log.info "Missing Component Capability : ${capability} Message : ${message}"
    }
    else log.info "Native Capability Message : ${message}"

    return evt
}

def configure(type) {  
    def children = getChildDevices()
    def childExists = false

    children.each { child ->
        if (!childExists) childExists = child.deviceNetworkId.contains(type.toString())   	
    }
    
    if (!childExists) {
        log.info "Adding capability ${type}"
        addChildDevice("domoticzSensor ${type}", 
                       "${device.displayName}-${type}", 
                       null, 
                       [completedSetup: true, label: "${device.displayName}-${type}", isComponent: true, componentName: "${type}", componentLabel: "${type}"])
	}                   

}
def updated() {
	initialize()
}

def initialize() {

	if (parent) {
        sendEvent(name: "DeviceWatch-Enroll", value: groovy.json.JsonOutput.toJson([protocol: "LAN", scheme:"untracked"]), displayed: false)
        if (device.currentValue("powerToday") == null) sendEvent(name:"powerToday", value:"Power not reported")
    }
    else {
    	log.error "You cannot use this DTH without the related SmartAPP Domoticz Server, the device needs to be a child of this App"
        sendEvent(name: "motion", value: "Error", descriptionText: "$device.displayName You cannot use this DTH without the related SmartAPP Domoticz Server", isStateChange: true)
    }
}