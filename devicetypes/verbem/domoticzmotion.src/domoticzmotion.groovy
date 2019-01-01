/**
 *  domoticzMotion
 *
 *  Copyright 2018 Martin Verbeek
 *
	V4.0 2018-06-02 Fix Power capability
 *
 */
metadata {
	definition (name: "domoticzMotion", namespace: "verbem", author: "SmartThings", vid : "generic-motion-7") {
    	capability "Configuration"
		capability "Motion Sensor"
		capability "Sensor"
		capability "Actuator"
		capability "Refresh"
		capability "Health Check"
       
        }

	tiles(scale: 2) {
		multiAttributeTile(name:"sensorMotion", type: "generic", width: 6, height: 4) {
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

		main "sensorMotion"
		details(["sensorMotion", "sensorTemperature", "sensorBattery", "sensorIlluminance", "sensorSignalStrength", "refresh"])

	}
}

def refresh() {

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
    
    def children = getChildDevices().findAll {it.deviceNetworkId.contains(type.toString()) == true}
    def DNI
   
    if (!children) {
    	DNI = device.deviceNetworkId.split(":")[2]
        log.info "Adding capability ${type} |${DNI}|"
        addChildDevice("domoticzSensor ${type}", "IDX:${DNI}-${type}", null, [completedSetup: true, label: "${device.displayName}-${type}", isComponent: true, componentName: "${type}", componentLabel: "${type}"])
	}                   
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
        sendEvent(name: "motion", value: "Error", descriptionText: "$device.displayName You cannot use this DTH without the related SmartAPP Domoticz Server", isStateChange: true)
    }
}