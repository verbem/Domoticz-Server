/**
 *  Domoticz OnOff SubType Switch.
 *
 *  SmartDevice type for domoticz switches and dimmers.
 *  
 *
 *  Copyright (c) 2018 Martin Verbeek
 *
 *  Revision History
 *  ----------------
 *	2018-03-03 7.10 Add Smart Screens as a parent for on/off, call refresh from poll
 *	2018-03-03 7.00 Component based, Graph, Mood, RSSI, BATTERY
 *	2018-01-28 6.00 Check state before issue on or off, dont with same state
 *	2017-12-31 5.02	Added power today and total to powerToday event, changed tile to display
 *	2017-12-15 5.00 implemented image graphs for power usage if available
 *  2017-10-28 4.12 correct to saturation color in normal setColor mode
 * 	2017-10-17 4.11 ColorTemperature added, added multi parent control for domoticz and hue sensor (connect) 
 *	2017-04-28 3.13 Color setting for White types
 *  2017-04-14 3.12 Multistate support for DZ selector
 *  2017-01-25 3.09 Put in check for switch name in generateevent
 *	2017-01-18 3.08 get always an lowercase value for switch on/off in generateevent
 *	2017-03-28 4.00	add Hue API effect/alert buttons
 *	2018-12-25 4.10 make connect compatible (work left on the child devices)
 */
 
import Calendar.*
import groovy.time.*

metadata {
    definition (name:"domoticzOnOff", namespace:"verbem", author:"Martin Verbeek", mnmn:"SmartThings", vid: "generic-rgbw-color-bulb") {
    	capability "Configuration"
        capability "Actuator"
        capability "Sensor"
        capability "Color Control"
		capability "Color Temperature"
		capability "Switch"
        capability "Switch Level"
        capability "Refresh"
		capability "Health Check"
        capability "Power Meter"
        capability "Energy Meter"
        capability "Power Consumption Report"
    }

    tiles(scale:2) {
    	multiAttributeTile(name: "richDomoticzOnOff", type: "lighting",  width:6, height:4, canChangeIcon: true, canChangeBackground: true) {
        	tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label:'Off', icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", action:"on", nextState:"Turning On"
                attributeState "Off", label:'Off', icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", action:"on", nextState:"Turning On"
                attributeState "OFF", label:'Off',icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", action:"on", nextState:"Turning On"
                attributeState "Turning Off", label:'Turning Off', icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"Turning On"
                
                attributeState "on", label:'On', icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", action:"off", nextState:"Turning Off"
                attributeState "On", label:'On', icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", action:"off", nextState:"Turning Off"
                attributeState "ON", label:'On', icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", action:"off", nextState:"Turning Off"
                attributeState "Set Level", label:'On', icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", action:"off", nextState:"Turning Off"
                attributeState "Turning On", label:'Turning On', icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"Turning Off"
				//LightWaveRF Mood button states                
                attributeState "Group Off", label:'Group Off', icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzonoffbutton.src/LigthWaveRFOff.png", backgroundColor:"#00a0dc", action:"off"
                attributeState "Group Mood 1", label:'Mood 1', icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzonoffbutton.src/LightWaveRFMood.png", backgroundColor:"#00a0dc", action:"off"
                attributeState "Group Mood 2", label:'Mood 2', icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzonoffbutton.src/LightWaveRFMood.png", backgroundColor:"#00a0dc", action:"off"
                attributeState "Group Mood 3", label:'Mood 3', icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzonoffbutton.src/LightWaveRFMood.png", backgroundColor:"#00a0dc", action:"off"
                attributeState "Group Mood 4", label:'Mood 4', icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzonoffbutton.src/LightWaveRFMood.png", backgroundColor:"#00a0dc", action:"off"
                attributeState "Group Mood 5", label:'Mood 5', icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzonoffbutton.src/LightWaveRFMood.png", backgroundColor:"#00a0dc", action:"off"
           
				attributeState "Error", label:'Install Error', backgroundColor: "#bc2323"
                
            }
            tileAttribute ("device.power", key: "SECONDARY_CONTROL") {
                attributeState "power", label:'${currentValue} W', icon: "st.Appliances.appliances17"
            }            
            tileAttribute("device.level", key: "SLIDER_CONTROL") {
            	attributeState "level", label:'${currentValue}', action:"setLevel" 
            }
            tileAttribute ("device.color", key: "COLOR_CONTROL") {
        		attributeState "color", action:"setColor"
            }
        }
        
        valueTile("powerConsumptionTile", "device.powerConsumption", width: 6, height: 2) {
        	state "powerConsumption", label:'${currentValue}', defaultState: true
    	}

		standardTile("refresh", "device.motion", decoration: "flat", width:1, height:1) {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
		childDeviceTile("Group Off", "Group Off", decoration: "flat", width: 2, height: 1, childTileName: "stateButton")
		childDeviceTile("Group Mood 1", "Group Mood 1", decoration: "flat", width: 2, height: 1, childTileName: "stateButton")
		childDeviceTile("Group Mood 2", "Group Mood 2", decoration: "flat", width: 2, height: 1, childTileName: "stateButton")
		childDeviceTile("Group Mood 3", "Group Mood 3", decoration: "flat", width: 2, height: 1, childTileName: "stateButton")
		childDeviceTile("Group Mood 4", "Group Mood 4", decoration: "flat", width: 2, height: 1, childTileName: "stateButton")
		childDeviceTile("Group Mood 5", "Group Mood 5", decoration: "flat", width: 2, height: 1, childTileName: "stateButton")
        
		childDeviceTile("Effect None", "Effect None", decoration: "flat", width: 2, height: 1, childTileName: "stateButton")
		childDeviceTile("Effect Colorloop", "Effect Colorloop", decoration: "flat", width: 2, height: 1, childTileName: "stateButton")
		childDeviceTile("Alert None", "Alert None", decoration: "flat", width: 2, height: 1, childTileName: "stateButton")
		childDeviceTile("Alert Select", "Alert Select", decoration: "flat", width: 2, height: 1, childTileName: "stateButton")
		childDeviceTile("Alert Lselect", "Alert Lselect", decoration: "flat", width: 2, height: 1, childTileName: "stateButton")

		childDeviceTile("sensorSignalStrength", "Signal Strength", decoration: "flat", width: 1, height: 1, childTileName: "sensorSignalStrength")   
		childDeviceTile("sensorBattery", "Battery", decoration: "flat", width: 2, height: 2, childTileName: "sensorBattery")   

        main(["richDomoticzOnOff"])
        details(["richDomoticzOnOff", "Group Off", "Group Mood 1", "Group Mood 2", "Group Mood 3", "Group Mood 4", "Group Mood 5",
        	"Effect None", "Effect Colorloop", "Alert None", "Alert Select", "Alert Lselect",
        	"sensorSignalStrength", "sensorBattery",  "powerConsumptionTile", "refresh"])
    }
}

// switch.refresh() command handler
def refresh() {

    if (parent.name == "Domoticz Server") {
    	parent.domoticz_poll(getIDXAddress())
    }
    
    if (parent.name == "Hue Sensor (Connect)") parent.groupCommand(["command" : "poll", "dni": device.deviceNetworkId]) 

}

// switch.on() command handler
def on() {
	if (device.currentValue("switch")?.toUpperCase() == "ON") return
	log.info parent.name   
    if (parent.name == "Smart Screens") sendEvent(name: "switch", value: "on")
    if (parent.name == "Domoticz Server") parent.domoticz_on(getIDXAddress())
    if (parent.name == "Hue Sensor (Connect)") parent.groupCommand(["command" : "on", "dni": device.deviceNetworkId])
}

// switch.off() command handler
def off() {
	if (device.currentValue("switch")?.toUpperCase() == "OFF") return
    
    if (parent.name == "Smart Screens") sendEvent(name: "switch", value: "off")
    if (parent.name == "Domoticz Server") parent.domoticz_off(getIDXAddress())
    if (parent.name == "Hue Sensor (Connect)") parent.groupCommand(["command" : "off", "dni": device.deviceNetworkId])
}

// Custom setlevel() command handler
def setLevel(level) {
    
    TRACE("setLevel Level " + level)
    state.setLevel = level

    if (parent.name == "Domoticz Server") parent.domoticz_setlevel(getIDXAddress(), level)
    if (parent.name == "Hue Sensor (Connect)") parent.groupCommand(["command" : "level", "dni": device.deviceNetworkId, "level": level])
}

def setColorTemperature(ct) {
    //if (parent.name == "Domoticz Server") parent.domoticz_setlevel(getIDXAddress(), level)
    if (parent.name == "Hue Sensor (Connect)") parent.groupCommand(["command" : "colorTemperature", "dni": device.deviceNetworkId, "ct": ct])

}

// Custom setcolor() command handler hue from ST is percentage of 360 which is max HUE
def setColor(color) {  
	log.trace "[setColor] " + color
    
    if (color.switch == "off") {
    	off()
    	return
    }
    
	def hexCode = null
    def colorRGB
    
    if (!color.level ) color.level = 50
    if (color.saturation && color.hue) {
    	colorRGB = hslToRgb(color.hue, color.saturation, color.level)
        log.trace "HSL to HEX " + colorUtil.rgbToHex(colorRGB.r.toInteger(),colorRGB.g.toInteger(),colorRGB.b.toInteger())-"#" + " " +  colorRGB
    }
    if (color.red && color.green && color.blue) log.trace "RGB to HEX " + colorUtil.rgbToHex(color.red.toInteger(),color.green.toInteger(),color.blue.toInteger())-"#"
    
    if (!color?.hex) {
        //hue:83, saturation:100, level:80
        if (parent) {
        
            colorRGB = hslToRgb(color.hue, color.saturation, color.level)
            hexCode = colorUtil.rgbToHex(colorRGB.r.toInteger(),colorRGB.g.toInteger(),colorRGB.b.toInteger())-"#" 
        	TRACE("SetColor (ST Cloud) HEX " + hexCode + " Sat " + Math.round(color.saturation) + " Level " + state.setLevel)
            
            if (colorRGB.a == true) "Achromatic request"
			hexCode = null
            
            if (color.hue == 5 && color.saturation == 4) {
                hexCode = "FEFFFA"
                log.debug "Soft White - Default ${hexCode}"
            }
            else if (color.hue == 63 && color.saturation == 28) {
                hexCode = "EFF9FF"
                log.debug "White - Concentrate ${hexCode}"
            }
            else if (color.hue == 63 && color.saturation == 43) {
                hexCode = "FAFDFF"
                log.debug "Daylight - Energize ${hexCode}"
            }
            else if (color.hue == 79 && color.saturation == 7) {
                hexCode = "FFFAEE"
                log.debug "Warm White - Relax ${hexCode}"
            }

            //else if (color.saturation ==0) { // achromatic
            //	colorRGB = hslToRGB(color.hue, color.saturation, color.level)
            //	hexCode = colorUtil.rgbToHex(color.level, color.level, color.level)
            //}
            
            if (hexCode == null) {
                log.trace "normal"
                if (parent.name == "Domoticz Server") parent.domoticz_setcolorHue(getIDXAddress(), (color.hue*3.6), Math.round(color.saturation), color.level)
                if (parent.name == "Hue Sensor (Connect)") parent.groupCommand(["command" : "hue", "dni": device.deviceNetworkId, "level": color.level, "hue": color.hue, "sat": color.saturation])                           
            }
            else {
                log.trace "whitelevel"
                if (parent.name == "Domoticz Server") parent.domoticz_setcolorWhite(getIDXAddress(), hexCode, Math.round(color.saturation), color.level)
                if (parent.name == "Hue Sensor (Connect)") parent.groupCommand(["command" : "white", "dni": device.deviceNetworkId, "level": color.level, "hue": color.hue, "sat": Math.round(color.saturation)])                           
            }
        }
    }
    else {
        TRACE("SetColor (device color picker) HEX " + color.hex - "#" + " Sat " + Math.round(color.saturation) + " Level " + state.setLevel)
        if (parent) {
            if (parent.name == "Domoticz Server") parent.domoticz_setcolor(getIDXAddress(), color.hex - "#", Math.round(color.saturation), state.setLevel)
            if (parent.name == "Hue Sensor (Connect)") parent.groupCommand(["command" : "hue", "dni": device.deviceNetworkId, "level": state.setLevel, "hue": color.hue, "sat": color.saturation])                        
        }
    }
    sendEvent(name: "color", value: [hue: color.hue.toInteger(), saturation: color.saturation.toInteger()])
    sendEvent(name: "switch", value: "on")

}

def getWhite(value) {
	log.debug "getWhite($value)"
	def level = Math.min(value as Integer, 99)    
    level = 255 * level/99 as Integer
	log.debug "level: ${level}"
	return level
}

def parse(Map message) {
	def evt = createEvent(message)
    def children = getChildDevices()
    
    if (message.name == "button") {
    	sendEvent(name: "switch", value: message.value)
        children.each { child ->
        	if (child.displayName.split("=")[1] == message.value) child.sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], descriptionText: "$device.displayName button $message.value was pushed", isStateChange: true)   	
    	}
    }

    def capability
    if (message.name.matches("rssi|battery")) {

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
            children.each { child ->
                if (child && child.displayName.split("=")[1] == capability) { 
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

//LIGHTWAVERF MOOD
def callMood(moodCommand) {
	sendEvent(name: "switch", value: moodCommand)
	parent.domoticz_mood(getIDXAddress(), moodCommand)
}
//END LIGHTWAVERF

//HUE API EFFECT or ALERT
def callEffect(hueCommand) {
	
    switch(hueCommand) {
        case "Effect None":
        	hueCommand = ["effect":"none", "alert":"none"]
            break
        case "Effect Colorloop":
        	hueCommand = ["effect":"colorloop", "alert":"none"]
            break
        case "Alert None":
        	hueCommand = ["effect":"none", "alert":"none"]
            break
        case "Alert Select":
        	hueCommand = ["effect":"none", "alert":"select"]
            break
        case "Alert Lselect":
        	hueCommand = ["effect":"none", "alert":"lselect"]
            break
	}
	parent.configHueRoomEffects([dni:getIDXAddress(), effect: hueCommand])
}
//END LIGHTWAVERF


def configure(type) {  

    def children = getChildDevices()
    def childExists = false
    def devType = "domoticzOnOff"
    def subType = type.toString()

    children.each { child ->
        if (!childExists) childExists = child.deviceNetworkId.contains(type.toString())   	
    }
    
	if (type.toString().contains("Effect")) type = "Button"
	if (type.toString().contains("Alert")) type = "Button"
	if (type.toString().contains("Group")) type = "Button"
    
	if (type.toString().matches("Signal Strength|Battery")) devType = "domoticzSensor "
    if (!childExists) {
        log.info "Adding capability ${subType}"
        try {
            addChildDevice("${devType}${type}", 
                           "${device.displayName}=${subType}", 
                           null, 
                           [completedSetup: true, label: "${device.displayName}=${subType}", isComponent: true, componentName: "${subType}", componentLabel: "${subType}"])
            }
        catch(e) {log.info "Error adding child ${devType}${type} ${device.displayName}=${subType} error ${e}"}
	}                   
}


private def TRACE(message) {
    log.debug message
}

private def STATE() {
    log.debug "switch is ${device.currentValue("switch")}"
    log.debug "deviceNetworkId: ${device.deviceNetworkId}"
}

// gets the IDX address of the device
private getIDXAddress() {
    
    def idx = getDataValue("idx")
        
    if (!idx) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 3) {
            idx = parts[2]
        } else {
            log.warn "Can't figure out idx for device: ${device.name} returning ${device.deviceNetworkId}"
            return device.deviceNetworkId.toString()
        }
    }

    //log.debug "Using IDX: $idx for device: ${device.id}"
    return idx
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
        //sendEvent(name: "powerToday", value: "noPower")
    }
    else {
    	log.error "You cannot use this DTH without the related SmartAPP Domoticz Server, the device needs to be a child of this App"
        sendEvent(name: "switch", value: "Error", descriptionText: "$device.displayName You cannot use this DTH without the related SmartAPP Domoticz Server", isStateChange: true)
    }
}

/**
     * Converts an HSL color value to RGB. Conversion formula
     * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
     * Assumes h, s, and l are contained in the set [0, 1] and
     * returns r, g, and b in the set [0, 255].
     *
     * @param   {number}  h       The hue
     * @param   {number}  s       The saturation
     * @param   {number}  l       The lightness
     * @return  {Array}           The RGB representation added with an indicator for achromatic true or false
     */
def hslToRgb(h, s, l){
		h = h / 100
        s = s / 100
        l = l / 100
        log.trace "H ${h}" + " S ${s}" + " L ${l}"
        def r, g, b, a;

        if(s == 0){
            r = g = b = l; // achromatic
            a = true
        }
        else{
           	def q = l < 0.5 ? l * (1 + s) : l + s - l * s;
            def p = 2 * l - q;
            r = hue2rgb(p, q, h + 1/3);
            g = hue2rgb(p, q, h);
            b = hue2rgb(p, q, h - 1/3);
            a = false
        }

        return [r: Math.round(r * 255), g: Math.round(g * 255), b: Math.round(b * 255), a: a];
    }
    
def hue2rgb(p, q, t){
    if(t < 0) t += 1;
    if(t > 1) t -= 1;
    if(t < 1/6) return p + (q - p) * 6 * t;
    if(t < 1/2) return q;
    if(t < 2/3) return p + (q - p) * (2/3 - t) * 6;
    return p;
}