/**
 *  domoticzSensor "Battery" component
 *
 *  Copyright 2018 Martin Verbeek
 *
	V7.00
 *
 */
metadata {
	definition (name: "domoticzSensor Battery", namespace: "verbem", author: "Martin Verbeek") {
		capability "Battery"
		capability "Sensor"
	}

	tiles
    {
		standardTile("sensorBattery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue} %', unit:"", icon:"https://raw.githubusercontent.com/verbem/SmartThingsPublic/master/devicetypes/verbem/domoticzsensor.src/battery.png"
			state "Error", label: "Install Error", backgroundColor: "#bc2323"
		}        
		main (["sensorBattery"])
		details(["sensorBattery"])
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
        sendEvent(name: "battery", value: 100)
    }
    else {
    	log.error "You cannot use this DTH without the related DTH domoticzSensor, the device needs to be a child of this DTH"
        sendEvent(name: "battery", value: "Error", descriptionText: "$device.displayName You cannot use this DTH without the related domoticzSensor DTH", isStateChange: true)
    }
  }
  catch (e) {
  	log.error e
  }
}