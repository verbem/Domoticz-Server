/**
 *  Domoticz Selector SubType Switch.
 *
 *  SmartDevice type for domoticz selector switches.
 *  
 *
 *  Copyright (c) 2016 Martin Verbeek
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  Revision History
 *  ----------------
 *	2018-01-09 5.04 Composite device, children are states now.
 *  2017-04-14 3.12 Multistate support for DZ selector
 *  2017-01-25 3.09 Put in check for switch name in generateevent
 *	2017-01-18 3.08 get always an lowercase value for switch on/off in generateevent
 */

metadata {
    definition (name:"domoticzSelector", namespace:"verbem", author:"Martin Verbeek") {
        capability "Actuator"
        capability "Sensor"
        capability "Switch"
        capability "Switch Level"
        capability "Refresh"
        capability "Polling"
        capability "Signal Strength"
		capability "Health Check"
        
        attribute "selector", "string"
        attribute "prevSelector", "string"
        attribute "selectorState", "string"
        
        // custom commands
       	command "setLevel"
    }

    tiles(scale:2) {
    	multiAttributeTile(name:"richDomoticzSelector", type:"generic",  width:6, height:4) {
        	tileAttribute("device.selectorState", key: "PRIMARY_CONTROL") {
                attributeState "selectorState", label:'${currentValue}', icon:"st.Electronics.electronics13", backgroundColor: "#00a0dc", defaultState: true
				attributeState "Off", label:'${name}', icon:"st.Electronics.electronics13", backgroundColor: "#ffffff"
				attributeState "Error", label:"Install Error", backgroundColor: "#bc2323"
            }
            
            tileAttribute("device.level", key: "SECONDARY_CONTROL") {
            	attributeState "level", label:'Selector level ${currentValue}', defaultState: true
            }
		}
     
		standardTile("rssi", "device.rssi", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "rssi", label:'Signal ${currentValue}', unit:"", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzsensor.src/network-signal.png"
		}
        
        standardTile("debug", "device.motion", inactiveLabel: false, decoration: "flat", width:2, height:2) {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }

		childDeviceTiles("stateButton", decoration: "flat", width: 2, height: 2)
        
        main(["richDomoticzSelector"])
        
        details(["richDomoticzSelector", "rssi", "debug", "stateButton"])
    }
}

// switch.poll() command handler
def poll() {

    if (parent) {
        TRACE("poll() ${device.deviceNetworkId}")
        parent.domoticz_poll(getIDXAddress())
    }
}

// switch.poll() command handler
def refresh() {

    if (parent) {
        parent.domoticz_poll(getIDXAddress())
    }
}

def on() {

    if (parent) {
        parent.domoticz_on(getIDXAddress())
    }
}

def off() {

    if (parent) {
        parent.domoticz_off(getIDXAddress())
    }
}

def setLevel(level) {

	level = level.toInteger()
    log.info "setLevel Level " + level
    state.setLevel = level
    def ix = level / 10
    def status = device.currentValue("selector").tokenize('|')
    sendEvent(name : 'selectorState', value : status[ix.toInteger()])
    if (parent) {
        parent.domoticz_setlevel(getIDXAddress(), level)
    }
}

private def TRACE(message) {
    log.debug message
}

private def STATE() {
    log.debug "Selector is ${device.currentValue("selectorState")}"
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
            log.warn "Can't figure out idx for device: ${device.id}"
        }
    }

    return idx
}

def updateChildren() {
	log.info "[updateChildren]"
    if (device.currentValue("selector") == null) return
    if (device.currentValue("prevSelector") == device.currentValue("selector")) return
    
    def newLevels = device.currentValue("selector").tokenize("|")
    def oldLevels
    def copyL

    if (device.currentValue("prevSelector") == "init") {
        oldLevels = device.currentValue("selector").tokenize("|")
        copyL = device.currentValue("selector").tokenize("|")
    }
    else {
        oldLevels = device.currentValue("prevSelector").tokenize("|")
        copyL = device.currentValue("prevSelector").tokenize("|")
    }
    
    sendEvent(name: "prevSelector", value: device.currentValue("selector")) 

    //first add all in newLevels
    newLevels.each { level ->
        def children = getChildDevices()
        def childExists = false

        children.each { child ->
            if (!childExists) childExists = child.deviceNetworkId.contains(level.toString())   	
        }

        if (!childExists) {
	        log.info "Adding ${level}"
            addChildDevice("domoticzSelectorState", 
            	"${device.displayName}-${level}", 
                null, 
                [completedSetup: true, label: "${device.displayName}-${level}", isComponent: true, componentName: "${level}", componentLabel: "${level}"])
        }

        copyL.each { oldLevel ->
            if (oldLevel == level) oldLevels.remove(oldLevel)
        }
    }
    
    //second delete all that are not used anymore
    def oldDni
    def noError = true

    oldLevels.each { oldLevel ->
        oldDni = "${device.displayName}-${oldLevel}"
        log.info "deleting ${oldDni}"
        try { deleteChildDevice(oldDni) }
        catch (e) {
            sendEvent(name: "selectorState", value: "Error", descriptionText: "Error deleting $oldLevel $e", isStateChange: true)
            noError = false
        }
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
        sendEvent(name: "prevSelector", value: "init") 
        updateChildren()
    	unschedule(updateChildren)
        runEvery5Minutes(updateChildren)
        def children = getChildDevices()
        
        children.each { child ->
            child.initialize()   	
        }
    }
    else {
    	log.error "You cannot use this DTH without the related SmartAPP Domoticz Server, the device needs to be a child of this App"
        sendEvent(name: "selectorState", value: "Error", descriptionText: "$device.displayName You cannot use this DTH without the related SmartAPP Domoticz Server", isStateChange: true)
    }
}