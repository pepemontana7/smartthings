 
def getVersionNum() { return "0.0.1" }
private def getVersionLabel() { return "GoThings Thermostat Version ${getVersionNum()}" }

 
metadata {
	definition (name: "GoThings ", namespace: "pepemontana7", author: "SmartThings") {
		capability "Actuator"
		capability "Thermostat"
        	capability "Sensor"
		capability "Refresh"
		capability "Execute"

		capability "Thermostat Operating State"
 
 
		command "generateEvent"
 		command "resumeProgram"
        
        	//command "setThermostatProgram"
        	command "home"
        	command "sleep"
        	command "away"
        
        	command "setStateVariable"
        
       	attribute "temperatureScale", "string"
 		attribute "thermostatStatus","string"
        	attribute "apiConnected","string"
        
		attribute "currentProgram","string"
           
        	attribute "debugEventFromParent","string"
        	attribute "logo", "string"
        	attribute "timeOfDate", "enum", ["day", "night"]
        	attribute "lastPoll", "string"
            attribute "ID", "number"
		    attribute "actions", "enum", ["shoot"]

        	attribute "Status", "string"
        	attribute "Reading", "number"

    
	}

	simulator { }

    	tiles(scale: 2) {      
              
		multiAttributeTile(name:"tempSummary", type:"thermostat", width:6, height:4) {
			tileAttribute("device.Reading", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'${currentValue}', unit:"dF")
			}


			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("No FLAME", backgroundColor:"#44b621")
				attributeState("FLAMES!!!", backgroundColor:"#ffa81e")
				attributeState("cooling", backgroundColor:"#269bd2")
			}


        } // End multiAttributeTile
        

        // Workaround until they fix the Thermostat multiAttributeTile. Only use this one OR the above one, not both
        multiAttributeTile(name:"summary", type: "lighting", width: 6, height: 4) {
        	tileAttribute("device.Reading", key: "PRIMARY_CONTROL") {
				attributeState("Reading", label:'${currentValue}°', unit:"dF",
				backgroundColors: getTempColors())
			}

        }

        // Show status of the API Connection for the Thermostat
		standardTile("apiStatus", "device.apiConnected", width: 2, height: 2) {
        	state "full", label: "API", backgroundColor: "#44b621", icon: "st.contact.contact.closed"
            state "warn", label: "API ", backgroundColor: "#FFFF33", icon: "st.contact.contact.open"
            state "lost", label: "API ", backgroundColor: "#ffa81e", icon: "st.contact.contact.open"
		}

		valueTile("currentStatus", "device.status", height: 2, width: 2, decoration: "flat") {
			state "status", label:'${currentValue}', backgroundColor:"#ffffff"
		}

		standardTile("refresh", "device.thermostatMode", width: 2, height: 2,inactiveLabel: false, decoration: "flat") {
            state "default", action:"refresh.refresh", label: "Refresh", icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/header_ecobeeicon_blk.png"
		}
        
		standardTile("execute", "device.ID", width: 2, height: 2,inactiveLabel: false, decoration: "flat") {
            state "default", action:"execute.execute", label: "SPRAY", icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/header_ecobeeicon_blk.png"
		}

        standardTile("resumeProgram", "device.resumeProgram", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "resume", action:"resumeProgram", nextState: "updating", label:'Resume', icon:"https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/action_resume_program.png"
			state "updating", label:"Working", icon: "st.samsung.da.oven_ic_send"
		}
        
  
        valueTile("currentProgram", "device.currentProgramName", height: 2, width: 4, inactiveLabel: false, decoration: "flat") {
			state "default", label:'Comfort Setting:\n${currentValue}' 
		}
 
        standardTile("operatingState", "device.thermostatOperatingState", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			//state "idle", label: "Idle", backgroundColor:"#44b621", icon: "st.nest.empty"
            state "idle", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/systemmode_idle.png"
            state "fan only", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/operatingstate_fan.png"
			state "heating", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/operatingstate_heat.png"
			state "cooling", icon: "https://raw.githubusercontent.com/StrykerSKS/SmartThings/master/smartapp-icons/ecobee/png/operatingstate_cool.png"
            // Issue reported that the label overlaps. Need to remove the icon
            state "default", label: '${currentValue}', icon: "st.nest.empty"
		}
         valueTile("lastPoll", "device.lastPoll", height: 2, width: 4, decoration: "flat") {
			state "thermostatStatus", label:'Last Poll:\n${currentValue}', backgroundColor:"#ffffff"
		}
    
    
		main([ "tempSummary"])
		details([
        	"tempSummary",
        	"operatingState", "execute", "refresh", 

            "lastPoll", "apiStatus"
            ])            
	}

	preferences {
    	section () {
			input "holdType", "enum", title: "Hold Type", description: "When changing temperature, use Temporary or Permanent hold (default)", required: false, options:["Temporary", "Permanent"]
        	// TODO: Add a preference for the background color for "idle"
        	// TODO: Allow for a "smart" Setpoint change in "Auto" mode. Why won't the paragraph show up in the Edit Device screen?
        	paragraph "The Smart Auto Temp Adjust flag allows for the temperature to be adjusted manually even when the thermostat is in Auto mode. An attempt to determine if the heat or cool setting should be changed will be made automatically."
            input "smartAuto", "bool", title: "Smart Auto Temp Adjust", description: true, required: false
            // input "detailedTracing", "bool", title: "Enable Detailed Tracing", description: true, required: false
       }
	}

}

// parse events into attributes
def parse(String description) {
	LOG( "parse() --> Parsing '${description}'" )
	// Not needed for cloud connected devices

}

def refresh() {
	LOG("refresh() called", 4)
	poll()
}

def execute(command = "spray" , args = "1") {
	LOG("Spary() called on  id ${device.currentValue('ID')}")
    LOG("Spary() called on actions ${device.currentValue('actions')}")
	parent.spray(this)
}
void poll() {
	LOG("Executing 'poll' using parent SmartApp")
    parent.pollChildren(this)
}

def generateEvent(Map results) {
    LOG("generateEvent(): parsing data $results")

	LOG("generateEvent(): parsing data $results", 4)
    LOG("Debug level of parent: ${parent.settings?.debugLevel}", 4, null, "debug")
	def linkText = getLinkText(device)
    LOG("generateEvent() in go things:  deviceID = ${getDeviceId()}" )
    LOG("generateEvent() in go things:  device   = ${device}" ) 
    LOG("generateEvent() in go things:  device.curerentValue('reading')   = ${device.currentValue('Reading')}" )

	if(results) {
		results.each { name, value ->
			LOG("generateEvent() - In each loop: name: ${name}  value: ${value}", 4)
			def isChange = false
			def isDisplayed = true
			def event = [name: name, linkText: linkText, descriptionText: getThermostatDescriptionText(name, value, linkText), handlerName: name]

			if (name=="temperature" || name=="heatingSetpoint" || name=="coolingSetpoint" || name=="weatherTemperature" ) {
				def sendValue = value // ? convertTemperatureIfNeeded(value.toDouble(), "F", 1): value //API return temperature value in F
                LOG("generateEvent(): Temperature value: ${sendValue}", 5, this, "trace")
				isChange = isTemperatureStateChange(device, name, value.toString())
				isDisplayed = isChange
				event << [value: sendValue, isStateChange: isChange, displayed: isDisplayed]
			} else if (name=="heatMode" || name=="coolMode" || name=="autoMode" || name=="auxHeatMode") {
				isChange = isStateChange(device, name, value.toString())
				event << [value: value.toString(), isStateChange: isChange, displayed: false]
			} else if (name=="thermostatOperatingState") {
            	generateOperatingStateEvent(value.toString())
                return
            } else if (name=="apiConnected") {
            	// Treat as if always changed to ensure an updated value is shown on mobile device and in feed
                isChange = isStateChange(device,name,value.toString());
                isDisplayed = isChange
                event << [value: value.toString(), isStateChange: isChange, displayed: isDisplayed]
            } else if (name=="weatherSymbol" && device.currentValue("timeOfDay") == "night") {
            	// Check to see if it is night time, if so change to a night symbol
                def symbolNum = value.toInteger() + 100
                isChange = isStateChange(device, name, symbolNum.toString())
                isDisplayed = isChange
				event << [value: symbolNum.toString(), isStateChange: isChange, displayed: isDisplayed]            
            } else {
				isChange = isStateChange(device, name, value.toString())
				isDisplayed = isChange
				event << [value: value.toString(), isStateChange: isChange, displayed: isDisplayed]
			}
			LOG("Out of loop, calling sendevent(${event})", 5)
			sendEvent(event)
		}
		//generateSetpointEvent()
		generateStatusEvent()
	}
}

//return descriptionText to be shown on mobile activity feed
private getThermostatDescriptionText(name, value, linkText) {
	if(name == "temperature") {
		return "$linkText temperature is ${value}°"

	} else if(name == "heatingSetpoint") {
		return "heating setpoint is ${value}°"

	} else if(name == "coolingSetpoint"){
		return "cooling setpoint is ${value}°"

	} else if (name == "thermostatMode") {
		return "thermostat mode is ${value}"

	} else if (name == "thermostatFanMode") {
		return "thermostat fan mode is ${value}"

	} else {
		return "${name} = ${value}"
	}
}

void resumeProgram() {
	// TODO: Put a check in place to see if we are already running the program. If there is nothing to resume, then save the calls upstream
	LOG("resumeProgram() is called", 5)
	sendEvent("name":"thermostatStatus", "value":"Resuming schedule...", "description":statusText, displayed: false)
	def deviceId = getDeviceId()
	if (parent.resumeProgram(this, deviceId)) {
		sendEvent("name":"thermostatStatus", "value":"Setpoint updating...", "description":statusText, displayed: false)
		runIn(15, "poll")
		LOG("resumeProgram() is done", 5)
		sendEvent("name":"resumeProgram", "value":"resume", descriptionText: "resumeProgram is done", displayed: false, isStateChange: true)
	} else {
		sendEvent("name":"thermostatStatus", "value":"failed resume click refresh", "description":statusText, displayed: false)
		LOG("Error resumeProgram() check parent.resumeProgram(this, deviceId)", 2, null, "error")
	}

	//generateSetpointEvent()
	generateStatusEvent()    
}

def generateQuickEvent(name, value) {
	generateQuickEvent(name, value, 0)
}

def generateQuickEvent(name, value, pollIn) {
	sendEvent(name: name, value: value, displayed: true)
    if (pollIn > 0) { runIn(pollIn, "poll") }
}

def generateFanModeEvent(fanMode) {
	sendEvent(name: "thermostatFanMode", value: fanMode, descriptionText: "$device.displayName fan is in ${mode} mode", displayed: true)
}

def generateOperatingStateEvent(operatingState) {
	LOG("generateOperatingStateEvent with state: ${operatingState}", 4)
	sendEvent(name: "thermostatOperatingState", value: operatingState, descriptionText: "$device.displayName is ${operatingState}", displayed: true)
}

def generateProgramEvent(program, failedProgram=null) {
	LOG("Generate generateProgramEvent Event: program ${program}" )

	sendEvent("name":"thermostatStatus", "value":"Setpoint updating...", "description":statusText, displayed: false)
	sendEvent("name":"currentProgramName", "value":program.capitalize())
    sendEvent("name":"currentProgramId", "value":program)
    
    def tileName = ""
    
    if (!failedProgram) {
    	tileName = "set" + program.capitalize()    	
    } else {
    	tileName = "set" + failedProgram.capitalize()    	
    }
    sendEvent("name":"${tileName}", "value":"${program}", descriptionText: "${tileName} is done", displayed: false, isStateChange: true)
}

def generateStatusEvent() {
   LOG("Generate Status Event = ${device}" )

	def mode = device.currentValue("thermostatMode")
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	def temperature = device.currentValue("temperature")

	def statusText	
	LOG("Generate Status Event for Mode = ${mode}", 4)
	LOG("Temperature = ${temperature}", 4)
	LOG("Heating setpoint = ${heatingSetpoint}", 4)
	LOG("Cooling setpoint = ${coolingSetpoint}", 4)
	LOG("HVAC Mode = ${mode}", 4)	

	if (mode == "heat") {
		if (temperature >= heatingSetpoint) {
			statusText = "Right Now: Idle"
		} else {
			statusText = "Heating to ${heatingSetpoint}°"
		}
	} else if (mode == "cool") {
		if (temperature <= coolingSetpoint) {
			statusText = "Right Now: Idle"
		} else {
			statusText = "Cooling to ${coolingSetpoint}°"
		}
	} else if (mode == "auto") {
		statusText = "Right Now: Auto (Heat: ${heatingSetpoint}/Cool: ${coolingSetpoint})"
	} else if (mode == "off") {
		statusText = "Right Now: Off"
	} else if (mode == "emergencyHeat" || mode == "emergency heat" || mode == "emergency") {
		statusText = "Emergency Heat"
	} else {
		statusText = "${mode}?"
	}
	LOG("Generate Status Event = ${statusText}" )
	sendEvent("name":"thermostatStatus", "value":statusText, "description":statusText, displayed: true)
}

//generate custom mobile activity feeds event
def generateActivityFeedsEvent(notificationMessage) {
	sendEvent(name: "notificationMessage", value: "$device.displayName $notificationMessage", descriptionText: "$device.displayName $notificationMessage", displayed: true)
}

def noOp() {
	// Doesn't do anything. Here due to a formatting issue on the Tiles!
}
 
private def getDeviceId() {
	def deviceId = device.deviceNetworkId.split(/\./).last()	
    LOG("getDeviceId() returning ${deviceId}", 4)
    return deviceId
}

private def usingSmartAuto() {
	LOG("Entered usingSmartAuto() ", 5)
	if (settings.smartAuto) { return settings.smartAuto }
    if (parent.settings.smartAuto) { return parent.settings.smartAuto }
    return false
}

private def whatHoldType() {
	def sendHoldType = parent.settings.holdType ? (parent.settings.holdType=="Temporary" || parent.settings.holdType=="Until Next Program")? "nextTransition" : (parent.settings.holdType=="Permanent" || parent.settings.holdType=="Until I Change")? "indefinite" : "indefinite" : "indefinite"
	LOG("Entered whatHoldType() with ${sendHoldType}  settings.holdType == ${settings.holdType}")
	if (settings.holdType && settings.holdType != "") { return  holdType ? (settings.holdType=="Temporary" || settings.holdType=="Until Next Program")? "nextTransition" : (settings.holdType=="Permanent" || settings.holdType=="Until I Change")? "indefinite" : "indefinite" : "indefinite" }   
   
    return sendHoldType
}

private debugLevel(level=3) {
	def debugLvlNum = parent.settings.debugLevel?.toInteger() ?: 3
    def wantedLvl = level?.toInteger()
    
    return ( debugLvlNum >= wantedLvl )
}


private def LOG(message, level=3, child=null, logType="debug", event=false, displayEvent=false) {
	def prefix = ""
	if ( parent.settings.debugLevel?.toInteger() == 5 ) { prefix = "LOG: " }
	if ( debugLevel(level) ) { 
    	log."${logType}" "${prefix}${message}"
        // log.debug message
        if (event) { debugEvent(message, displayEvent) }        
	}    
}

private def debugEvent(message, displayEvent = false) {

	def results = [
		name: "appdebug",
		descriptionText: message,
		displayed: displayEvent
	]
	if ( debugLevel(4) ) { log.debug "Generating AppDebug Event: ${results}" }
	sendEvent (results)
}

def getTempColors() {
	def colorMap

	colorMap = [
		// Celsius Color Range
		[value: 0, color: "#1e9cbb"],
		[value: 15, color: "#1e9cbb"],
		[value: 19, color: "#1e9cbb"],

		[value: 21, color: "#44b621"],
		[value: 22, color: "#44b621"],
		[value: 24, color: "#44b621"],

		[value: 21, color: "#d04e00"],
		[value: 35, color: "#d04e00"],
		[value: 37, color: "#d04e00"],
		// Fahrenheit Color Range
		[value: 40, color: "#1e9cbb"],
		[value: 59, color: "#1e9cbb"],
		[value: 67, color: "#1e9cbb"],

		[value: 69, color: "#44b621"],
		[value: 72, color: "#44b621"],
		[value: 74, color: "#44b621"],

		[value: 76, color: "#d04e00"],
		[value: 95, color: "#d04e00"],
		[value: 99, color: "#d04e00"]
	]
}