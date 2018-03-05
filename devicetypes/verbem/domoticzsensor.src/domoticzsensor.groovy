/**
 *  domoticzSensor Component Based Device Handler
 *
 *  Copyright 2018 Martin Verbeek
 *
 *
 */
metadata {
	definition (name: "domoticzSensor", namespace: "verbem", author: "SmartThings") {
    	capability "Configuration"
		capability "Temperature Measurement"
		capability "Sensor"
		capability "Actuator"
		capability "Refresh"
		capability "Health Check"
        capability "Thermostat"
        
    }

	tiles(scale: 2) {
       
		standardTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2, canChangeIcon: true) {
			state "temparature", label:'${currentValue} C', unit:"", icon:"st.Weather.weather2", 
							backgroundColors:[
							// Celsius
							[value: 0, color: "#153591"],
							[value: 7, color: "#1e9cbb"],
							[value: 15, color: "#90d2a7"],
							[value: 23, color: "#44b621"],
							[value: 28, color: "#f1d801"],
							[value: 35, color: "#d04e00"],
							[value: 37, color: "#bc2323"],
							// Fahrenheit
							[value: 40, color: "#153591"],
							[value: 44, color: "#1e9cbb"],
							[value: 59, color: "#90d2a7"],
							[value: 74, color: "#44b621"],
							[value: 84, color: "#f1d801"],
							[value: 95, color: "#d04e00"],
							[value: 96, color: "#bc2323"]
					]		
            state "Error", label:"Install Error", backgroundColor: "#bc2323"
        }

		standardTile("refresh", "device.refresh", decoration: "flat", inactiveLabel: false,  width: 2, height: 2) {
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
		
		main "temperature"
		details(["temperature", "sensorBattery", "sensorSignalStrength", "sensorHumidity", "sensorBarometricPressure", "sensorPower", "sensorIlluminance", "sensorAirQuality", "sensorSound", "sensorTemperature", "refresh"])
	}
}
def parse(Map message) {
	def evt = createEvent(message)
    def capability
    if (message.name.matches("airQuality|illuminance|soundPressureLevel|barometricPressure|humidity|battery|rssi")) {

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
        sendEvent(name: "temperature", value: "Error", descriptionText: "$device.displayName You cannot use this DTH without the related SmartAPP Domoticz Server", isStateChange: true)
    }
}