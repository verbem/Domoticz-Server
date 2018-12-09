/**
 *  Copyright 2018 Martin Verbeek
 *
 *
 *	Author: Martin Verbeek
 *	
 	V7.00	Initial release
    V7.16	Humidity, Temp and a more forgiving setting of thermostat modes.
 
 */
metadata {
	definition (name: "domoticzThermostat", namespace: "verbem", author: "Martin Verbeek") {
    	capability "Configuration"
		capability "Actuator"
		capability "Thermostat"
		capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
		capability "Sensor"
        capability "Switch"
		capability "Refresh"
		capability "Health Check"

		command "raiseSetpoint"
		command "lowerSetpoint"
		command "resumeProgram"

		attribute "maxHeatingSetpoint", "number"
		attribute "minHeatingSetpoint", "number"
		attribute "deviceTemperatureUnit", "string"
		attribute "deviceAlive", "enum", ["true", "false"]
	}
//**************************

multiAttributeTile(name:"thermostatFull", type:"thermostat", width:6, height:4) {
        tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
            attributeState("temperature", label:'${currentValue}', unit:"C", defaultState: true,
                        backgroundColors:[
                                [value: 10, color: "#153591"],
                                [value: 13, color: "#1e9cbb"],
                                [value: 16, color: "#90d2a7"],
                                [value: 19, color: "#44b621"],
                                [value: 22, color: "#f1d801"],
                                [value: 25, color: "#d04e00"],
                                [value: 30, color: "#bc2323"],
                        ]        
            )
        }
        tileAttribute("device.thermostatSetpoint", key: "VALUE_CONTROL") {
            attributeState("VALUE_UP", action:"raiseSetpoint", icon:"st.thermostat.thermostat-up")
            attributeState("VALUE_DOWN", action:"lowerSetpoint", icon:"st.thermostat.thermostat-down")
        }
        //tileAttribute("device.switch", key: "SECONDARY_CONTROL") {
        //    attributeState("On", label:'On', unit:"", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzblinds.src/Nefit Warm Water.PNG")
        //    attributeState("Off", label:'Off', unit:"", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzblinds.src/Nefit No Warm Water.PNG")
        //}
    	tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
        	attributeState("humidity", label:'${currentValue}%', unit:"%", defaultState: true)
    	}	
        tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
            attributeState("idle", backgroundColor:"#00A0DC")
            attributeState("heating", backgroundColor:"#e86d13")
            attributeState("cooling", backgroundColor:"#00A0DC")
        }
        tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
            attributeState("thermostatMode", label:'${currentValue}')
        }
        tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
            attributeState("coolingSetpoint", label:'${currentValue}', unit:"", defaultState: true)
        }
        tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
            attributeState("heatingSetpoint", label:'${currentValue}', unit:"", defaultState: true)
        }
    }
		childDeviceTile("sensorHumidity", "Relative Humidity Measurement", decoration: "flat", width: 2, height: 2, childTileName: "sensorHumidity")   
		childDeviceTile("sensorBarometricPressure", "Barometric Pressure", decoration: "flat", width: 2, height: 2, childTileName: "sensorBarometricPressure")   
		childDeviceTile("sensorPower", "Power Meter", decoration: "flat", width: 2, height: 2, childTileName: "sensorPower")   
		childDeviceTile("sensorGas", "Gas Meter", decoration: "flat", width: 2, height: 2, childTileName: "sensorGas")   
		childDeviceTile("sensorIlluminance", "Illuminance Measurement", decoration: "flat", width: 2, height: 2, childTileName: "sensorIlluminance")   
		childDeviceTile("sensorAirQuality", "Air Quality Sensor", decoration: "flat", width: 2, height: 2, childTileName: "sensorAirQuality")   
		childDeviceTile("sensorSound", "Sound Sensor", decoration: "flat", width: 2, height: 2, childTileName: "sensorSound")   
		childDeviceTile("sensorTemperature", "Temperature Measurement", decoration: "flat", width: 2, height: 2, childTileName: "sensorTemperature")   
		childDeviceTile("sensorSignalStrength", "Signal Strength", decoration: "flat", width: 2, height: 2, childTileName: "sensorSignalStrength")   
		childDeviceTile("sensorBattery", "Battery", decoration: "flat", width: 2, height: 2, childTileName: "sensorBattery")   

		standardTile("refresh", "device.refresh", decoration: "flat", inactiveLabel: false,  width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
		main "thermostatFull"
		details(["thermostatFull", "sensorBattery", "sensorSignalStrength", "sensorHumidity", "sensorBarometricPressure", "sensorGas", "sensorPower", "sensorIlluminance", "sensorAirQuality", "sensorSound", "sensorTemperature", "refresh"])
	

	//preferences {
	//	input "holdType", "enum", title: "Hold Type", description: "When changing temperature, use Temporary (Until next transition) or Permanent hold (default)", required: false, options:["Temporary", "Permanent"]
	//}
}
void setThermostatMode(setMode) {
	sendEvent(name: "thermostatMode", value: setMode)
    def thermostatOperatingState
    if (setMode != "emergency heat") {
    	if (setMode.toUpperCase().contains("HEAT")) setMode = "heat"
    	if (setMode.toUpperCase().contains("COOL")) setMode = "cool"
    	if (setMode.toUpperCase().contains("DRY")) setMode = "dry"
    	if (setMode.toUpperCase().contains("AUTO")) setMode = "auto"
    }
    
    log.info "Mode ${setMode} has been set"
   
    switch (setMode) {
    case "emergency heat":
		thermostatOperatingState = "heating"
    	break
    case "cool":
		thermostatOperatingState = "cooling"
		break
    case "heat":
		thermostatOperatingState = "heating"
    	break
    case "auto":
		thermostatOperatingState = "idle"
    	break
    case "eco":
		thermostatOperatingState = "idle"
    	break
    case "dry":
		thermostatOperatingState = "fan only"
    	break
    case "off":
		thermostatOperatingState = "idle"
    	break
   default:
		thermostatOperatingState = "idle"
    }
    sendEvent(name: "thermostatOperatingState", value: thermostatOperatingState)    	
}

void setThermostatFanMode(setMode) {
	sendEvent(name: "thermostatFanMode", value: setMode)
    log.info "Fan Mode ${setMode} has been set"
}


void installed() {
    // The device refreshes every 5 minutes by default so if we miss 2 refreshes we can consider it offline
    // Using 12 minutes because in testing, device health team found that there could be "jitter"
    sendEvent(name: "checkInterval", value: 60 * 12, data: [protocol: "cloud"], displayed: false)
}

// Device Watch will ping the device to proactively determine if the device has gone offline
// If the device was online the last time we refreshed, trigger another refresh as part of the ping.
def ping() {
    def isAlive = device.currentValue("deviceAlive") == "true" ? true : false
    if (isAlive) {
        refresh()
    }
}

def refresh() {
	poll()
}

void poll() {
	log.debug "Executing poll()"

    if (parent) {
        parent.domoticz_poll(getIDXAddress())
    }	
}

//return descriptionText to be shown on mobile activity feed
private getThermostatDescriptionText(name, value, linkText) {
	if(name == "temperature") {
		def sendValue =  value.toDouble()
		return "$linkText temperature is ${sendValue} ${location.temperatureScale}"

	} else if(name == "heatingSetpoint") {
		def sendValue =  value.toDouble()
		return "heating setpoint is ${sendValue} ${location.temperatureScale}"

	} else if (name == "thermostatMode") {
		return "thermostat mode is ${value}"

	} else {
		return "${name} = ${value}"
	}
}

void setCoolingSetpoint(setpoint) {
	def parts = device.deviceNetworkId.tokenize(":")
    def last = parts[parts.size()-1]
	log.debug "***cooling setpoint $setpoint for $last"
	parent.domoticz_setpoint(last, setpoint)  
    sendEvent(name: "coolingSetpoint", value: setpoint)
	//sendEvent(name: "thermostatStatus", value:"Cooling to ${setpoint}", description:"Heating to ${setpoint}", displayed: true)  
    sendEvent(name: "thermostatOperatingState", value: "cooling")


	return 
}

void setHeatingSetpoint(setpoint) {
	def parts = device.deviceNetworkId.tokenize(":")
    def last = parts[parts.size()-1]
	log.debug "***heating setpoint $setpoint for $last"
    
	parent.domoticz_setpoint(last, setpoint)  
    sendEvent(name: "heatingSetpoint", value: setpoint)
	//sendEvent(name: "thermostatStatus", value:"Heating to ${setpoint}", description:"Heating to ${setpoint}", displayed: true)    
    sendEvent(name: "thermostatOperatingState", value: "heating")
    
    return 
}

void resumeProgram() {
	log.debug "resumeProgram() is called"
}

def getDataByName(String name) {
	state[name] ?: device.getDataValue(name)
}


void raiseSetpoint() {
	def currentSetpoint = device.currentValue("thermostatSetpoint") + 0.5  
    log.info currentSetpoint
    parent.domoticz_setpoint(getIDXAddress(), currentSetpoint)
    sendEvent(name: "thermostatSetpoint", value: currentSetpoint)
}

//called by tile when user hit raise temperature button on UI
void lowerSetpoint() {
	def currentSetpoint = device.currentValue("thermostatSetpoint") - 0.5  
    log.info currentSetpoint
    parent.domoticz_setpoint(getIDXAddress(), currentSetpoint)
    sendEvent(name: "thermostatSetpoint", value: currentSetpoint)
}

def generateStatusEvent() {
	def mode = device.currentValue("thermostatMode")
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	def temperature = device.currentValue("temperature")
	def statusText

	log.debug "Generate Status Event for Mode = ${mode}"
	log.debug "Temperature = ${temperature}"
	log.debug "Heating set point = ${heatingSetpoint}"
	log.debug "HVAC Mode = ${mode}"

	if (mode == "manual") {
		if (temperature >= heatingSetpoint)
			statusText = "Right Now: Manual"
		else
			statusText = "Heating to ${heatingSetpoint} ${location.temperatureScale}"
	} else if (mode == "auto") {
		statusText = "Right Now: Auto"
	} else if (mode == "eco") {
		statusText = "Right Now: Eco"
	} else if (mode == "holiday") {
		statusText = "Right Now: Holiday"
	} else {
		statusText = "?"
	}

	log.debug "Generate Status Event = ${statusText}"
	//sendEvent("name":"thermostatStatus", "value":statusText, "description":statusText, displayed: true)
}

def generateActivityFeedsEvent(notificationMessage) {
	sendEvent(name: "notificationMessage", value: "$device.displayName $notificationMessage", descriptionText: "$device.displayName $notificationMessage", displayed: true)
}

def roundC (tempC) {
	return (Math.round(tempC.toDouble() * 2))/2
}

def convertFtoC (tempF) {
	return ((Math.round(((tempF - 32)*(5/9)) * 2))/2).toDouble()
}

def convertCtoF (tempC) {
	return (Math.round(tempC * (9/5)) + 32).toInteger()
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

    return idx
}

def parse(Map message) {
	
    def capability
    def evt = createEvent(message)
    if (message.name.matches("humidity|power|gas|temperature|battery|rssi")) {

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
        	case "gas":
            capability = "Gas Meter"
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
    log.info "Native Capability Message : ${message}"

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