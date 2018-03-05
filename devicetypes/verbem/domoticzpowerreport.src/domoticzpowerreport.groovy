/**
 *  domoticzPowerReport
 *
 *  Copyright 2017 Martin Verbeek
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
	definition (name: "domoticzPowerReport", namespace: "verbem", author: "Martin Verbeek") {
		capability "Sensor"
		capability "Actuator"
		capability "Health Check"
		capability "Power Meter"
        capability "Image Capture"
        
        attribute "powerTotal", "string"
        
        command takeDay
        command takeMonth
        command takeYear
        }
        
	tiles(scale: 2) {       
        multiAttributeTile(name:"powerReport", type:"generic", width:6, height:4) {
            tileAttribute("device.power", key: "PRIMARY_CONTROL") {
                attributeState "level", label:'${currentValue}', unit: "W" , defaultState: true, action: "take", backgroundColors:[                   
                    [value: 0, color: "#153591"],      	//dark blue
                    [value: 1000, color: "#1e9cbb"],    //light blue
                    [value: 2200, color: "#90d2a7"],	//greenish
                    [value: 3300, color: "#44b621"],	//dark green
                    [value: 5000, color: "#f1d801"],	//dark yellow
                    [value: 7500, color: "#d04e00"],	//dark orange
                    [value: 10000, color: "#bc2323"]	//red
                ]
            }
            tileAttribute("device.powerTotal", key: "SECONDARY_CONTROL") {
                attributeState "powerTotal", label:'${currentValue}', unit: "kWh"
            }
        }
        standardTile("powerMain", "device.power", inactiveLabel: false, decoration: "flat", width:2, height:2) {
            state "level", label:'${currentValue} W'
        }	        
        standardTile("day", "day", inactiveLabel: false, decoration: "flat", width:2, height:2) {
            state "default", label:'24 hours', action:"takeDay"
        }		
        standardTile("month", "month", inactiveLabel: false, decoration: "flat", width:2, height:2) {
            state "default", label:'Month', action:"takeMonth"
        }		
        standardTile("year", "year", inactiveLabel: false, decoration: "flat", width:2, height:2) {
            state "default", label:'Year', action:"takeYear"
        }		
        carouselTile("graph", "device.image", width: 6, height: 4)
	}

	main "powerReport"
    details(["powerReport", "day", "month", "year", "graph"])
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

def takeDay() {
	def type = device.deviceNetworkId.split(":")[1].split()[0]
    def copyState 
	state.chd = "t:"
    state.chxl = "0:%7C"
	state.chtt = "24%20hours"
    state.chco = "0000FF"
    state.chdl = device.currentState("power").unit
    
    if (type == "Power") {
        if (!parent.state.reportPowerDay || parent.state.reportPowerDay.size() == 0) return
		copyState = parent.state.reportPowerDay        
    }
    else if (type == "Gas") {
        if (!parent.state.reportGasDay || parent.state.reportGasDay.size() == 0) return
    	copyState = parent.state.reportGasDay
    }
    
    copyState.sort().each { key, item ->
        state.chd = state.chd + item + "," 
        state.chxl = state.chxl + key.split()[0] + "%20" + key.split()[1] + "%7C"
    }
    
    state.chd = state.chd + 0
    state.chxl = state.chxl + "%7C"
    take()
}

def takeMonth() {
	def type = device.deviceNetworkId.split(":")[1].split()[0]
    def copyState 
	state.chd = "t:"
    state.chxl = "0:%7C"
	state.chtt = "Month"
    state.chco = "0000FF"
    state.chdl = device.currentState("powerTotal").unit

    if (type == "Power") {
        if (!parent.state.reportPowerMonth || parent.state.reportPowerMonth.size() == 0) return
		copyState = parent.state.reportPowerMonth        
    }
    else if (type == "Gas") {
        if (!parent.state.reportGasMonth || parent.state.reportGasMonth.size() == 0) return
    	copyState = parent.state.reportGasMonth
    }
    
    copyState.sort().each { key, item ->
        state.chd = state.chd + item + "," 
        state.chxl = state.chxl + key + "%7C"
    }
    
    state.chd = state.chd + 0
    state.chxl = state.chxl + "%7C"
    take()
}

def takeYear() {
	def type = device.deviceNetworkId.split(":")[1].split()[0]
    def copyState 
	state.chd = "t:"
    state.chxl = "0:%7C"
	state.chtt = "Year"
    state.chco = "0000FF"
    state.chdl = device.currentState("powerTotal").unit
    
    if (type == "Power") {
        if (!parent.state.reportPowerYear || parent.state.reportPowerYear.size() == 0) return
		copyState = parent.state.reportPowerYear        
    }
    else if (type == "Gas") {
        if (!parent.state.reportGasYear || parent.state.reportGasYear.size() == 0) return
    	copyState = parent.state.reportGasYear
    }
    
    copyState.sort().each { key, item ->
        state.chd = state.chd + item + "," 
        state.chxl = state.chxl + key + "%7C"
    }
    
    state.chd = state.chd + 0
    state.chxl = state.chxl + "%7C"
    take()
}

def take() {

	def imageCharts = "https://image-charts.com/chart?"
	def params = [uri: "${imageCharts}chs=720x480&chd=${state.chd}&cht=bvg&chds=a&chxt=x,y&chxl=${state.chxl}&chts=0000FF,20&chco=${state.chco}&chtt=${state.chtt}&chdl=${state.chdl}"]
    
    if (state.imgCount == null) state.imgCount = 0
 
    try {
        httpGet(params) { response ->
        	
            if (response.status == 200 && response.headers.'Content-Type'.contains("image/png")) {
                def imageBytes = response.data
                if (imageBytes) {
                    state.imgCount = state.imgCount + 1
                    def name = "PowerUsage$state.imgCount"

                    // the response data is already a ByteArrayInputStream, no need to convert
                    try {
                        storeImage(name, imageBytes, "image/png")
                    } catch (e) {
                        log.error "error storing image: $e"
                    }
                }
            }
        else log.error "wrong format of content"
        }
    } catch (err) {
        log.error ("Error making request: $err")
    }
}