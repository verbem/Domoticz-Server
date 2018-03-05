/**
 *  Copyright 2015 SmartThings
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
 *  Life360-User
 *
 *  Author: jeff
 *  Date: 2013-08-15
 */
 
metadata {
	definition (name: "Life360 User", namespace: "verbem", author: "Martin Verbeek") {
		capability "Presence Sensor"
		capability "Sensor"
        capability "Image Capture"
        
        attribute "place", "string"
	}

	simulator {
		status "present": "presence: 1"
		status "not present": "presence: 0"
	}

	tiles {
		standardTile("presence", "device.presence", width: 3, height: 2, canChangeBackground: true) {
			state("present", labelIcon:"st.presence.tile.mobile-present", backgroundColor:"#00A0DC", action: "take")
			state("not present", labelIcon:"st.presence.tile.mobile-not-present", backgroundColor:"#ffffff", action: "take")
		}
 		valueTile("place", "place", decoration: "flat", width: 3, height: 1) {
            state "place", label:'Last seen at ${currentValue}'
        }
        
        carouselTile("currentLocation", "device.image", width: 3, height: 3)
        
		main "presence"
		details(["presence", "place", "currentLocation"])
	}
}

def generatePresenceEvent(boolean present) {
	log.info "Life360 generatePresenceEvent($present)"
	def value = formatValue(present)
	def linkText = getLinkText(device)
	def descriptionText = formatDescriptionText(linkText, present)
	def handlerName = getState(present)

	def results = [
		name: "presence",
		value: value,
		unit: null,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: handlerName
	]
	log.debug "Generating Event: ${results}"
	sendEvent (results)
}

def take() {
	log.debug "Take()"
    //def child = parent.getChildDevice(device.id)
    def userId = device.deviceNetworkId
    
    def coordinates = parent.refreshMembers(userId.tokenize('.')[1].toString())
    
    def lat = coordinates.lat
    def lon = coordinates.lon
    
	def params = [uri: "https://maps.googleapis.com/maps/api/staticmap?center=${lat},${lon}&zoom=15&size=400x400&scale=2&maptype=hybrid&format=jpg&markers=color:red%7Clabel:X%7C${lat},${lon}&key=AIzaSyCvNfXMaFmrTlIwIqILm7reh_9P-Sx3x2I"]
    if (state.imgCount == null) state.imgCount = 0
    
    try {
        httpGet(params) { response ->
        	
            if (response.status == 200 && response.headers.'Content-Type'.contains("image/jpeg")) {
                def imageBytes = response.data
                if (imageBytes) {
                    state.imgCount = state.imgCount + 1
                    def name = "test$state.imgCount"

                    // the response data is already a ByteArrayInputStream, no need to convert
                    try {
                        storeImage(name, imageBytes, "image/jpeg")
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

def setMemberId (String memberId) {
   log.debug "MemberId = ${memberId}"
   state.life360MemberId = memberId
}

def getMemberId () {

	log.debug "MemberId = ${state.life360MemberId}"
    
    return(state.life360MemberId)
}

private String formatValue(boolean present) {
	if (present)
    	return "present"
	else
    	return "not present"
}

private formatDescriptionText(String linkText, boolean present) {
	if (present)
		return "Life360 User $linkText has arrived"
	else
    	return "Life360 User $linkText has left"
}

private getState(boolean present) {
	if (present)
		return "arrived"
	else
    	return "left"
}