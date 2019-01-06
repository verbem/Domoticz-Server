/**
 *  domoticzSelector "State" component
 *
 *  Copyright 2019 Martin Verbeek
 *
 * 	7.20	Changed the way levels are validated, as the - char will interfere when it is part of statelevel
 *
 */
metadata {
	definition (name: "domoticzSelectorState", namespace: "verbem", author: "Martin Verbeek", vid:"generic-button") {
		capability "Button"
		capability "Actuator"
		capability "Sensor"
        capability "Switch"
        capability "Health Check"
        
        command "buttonPress"
        attribute "labelButton", "string"
	}

	tiles
    {
		standardTile("stateButton", "device.labelButton", decoration: "flat", width: 2, height: 2) {
			state "labelButton", label: '${currentValue}', action: "buttonPress"
			state "Error", label: "Install Error", backgroundColor: "#bc2323"
		}        
		main (["stateButton"])
		details(["stateButton"])
	}
}

def on() {
	buttonPress()
}

def buttonPress() {
    def stateLevels = device.displayName.tokenize("-")
    def stateLevel = (device.displayName - stateLevels[0]).substring(1)
    stateLevels = parent.currentValue("selector").tokenize("|")
    def ix = 0
    def found = 200
    stateLevels.each {
    	if (it == stateLevel) {
        	found = ix
        }
        ix = ix + 10
    }
    
    if (found != 200) {
        parent.setLevel(found)
    }
    else log.error "State not found "
}

def installed() {
	initialize()
}

def updated() {
	initialize()
}

def initialize() {
	log.info "Init"
    try {
    if (parent) {
        def stateLevels = device.displayName.tokenize("-")
        def stateLevel = (device.displayName - stateLevels[0]).substring(1)
        sendEvent(name: "labelButton", value: stateLevel)
    }
    else {
    	log.error "You cannot use this DTH without the related DTH domoticzSelector, the device needs to be a child of this DTH"
        sendEvent(name: "button", value: "Error", descriptionText: "$device.displayName You cannot use this DTH without the related SmartAPP Hue Sensor (Connect)", isStateChange: true)
    }
  }
  catch (e) {
  	log.error e
  }
}