/**
 *  Hubitat Import URL: https://raw.githubusercontent.com/HubitatCommunity/AverageThis/master/AverageThis.groovy
 *
 *  Average Illuminance, temperature, Relative Humidity
 *
 *  Copyright 2019 C Steele
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
 public static String version()      {  return "v1.1"  }

definition (
	name: "AverageThisChild",
	namespace: "csteele",
	author: "C Steele",
	description: "Child: Average a set of Omni sensors.",
	category: "Averaging",
	parent: "csteele:AverageThis",
	iconUrl: "",
	iconX2Url: "",
	iconX3Url: "",
)


// Preference pages
preferences
{
	page(name: "mainPage")
}



def subscribeSelected() {
	if (debugOutput) log.debug "subscribeSelected: $illumSensors"
	if (illumSensors?.size()) 
	{
	    subscribe(illumSensors, "illuminance", illumHandler)
	    subscribe(illumSensors, "temperature", tempHandler)
	    subscribe(illumSensors, "humidity", humidHandler)
	}
}

def illumHandler(evt) {
	def NSample = numberOption as Integer
	if (debugOutput) log.debug "illumHandler: $evt.device, $evt.value, ($state.avgL)"
	/* exponentially weighted average
	    avg -= avg / N;
    	    avg += new_sample / N;	
	 when N=1 avg = the latest value. When N > 100 is approximately the number of previous values to average
	 */	 
	def float avL = state.avgL
	avL -= avL / NSample
	avL += Float.parseFloat(evt.value) / NSample
	state.avgL = avL
	
	if(vDevice.supportedCommands.find{it.toString() == "setIlluminance"}) { settings.vDevice.setIlluminance("${state.avgL.round(1)}"); sendEvent(name: "illuminance", value: state.avgL, displayed: true)  }
	else { log.warn "Is Incorrect vDevice" }
}

def tempHandler(evt) {
	def NSample = numberOption as Integer
	if (debugOutput) log.debug "tempHandler: $evt.device, $evt.value, ($state.avgT)"
	def float avT = state.avgT
	avT -= avT / NSample
	avT += Float.parseFloat(evt.value) / NSample
	state.avgT = avT
	
	if(vDevice.supportedCommands.find{it.toString() == "setTemperature"}) { settings.vDevice.setTemperature("${state.avgT.round(1)}"); sendEvent(name: "temperature", value: state.avgT, displayed: true)  }
	else { log.warn "Is Incorrect vDevice" }
}

def humidHandler(evt) {
	def NSample = numberOption as Integer
	if (debugOutput) log.debug "humidHandler: $evt.device, $evt.value, ($state.avgH)"
	def float avH = state.avgH
	avH -= avH / NSample
	avH += Float.parseFloat(evt.value) / NSample
	state.avgH = avH
	
	if(vDevice.supportedCommands.find{it.toString() == "setRelativeHumidity"}) { settings.vDevice.setRelativeHumidity("${state.avgH.round(1)}"); sendEvent(name: "humidity", value: state.avgH, displayed: true)  }
	else { log.warn "Is Incorrect vDevice" }
}


def installed() {
	log.info "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.info "Updated with settings: ${settings}"
	unschedule()
	unsubscribe()
	if (debugOutput) runIn(1800,logsOff)
	if (debugOutput) log.debug "   Supported Commands of $vDevice:${vDevice.supportedCommands}"
	schedule("0 0 8 ? * FRI *", updateCheck)
	initialize()
}

def initialize() {
	version()
	// an inital value of 0 will take a long time to average out, thus avg is initialized to an arbitrary indoor average
	if (state.avgL == null) state.avgL = 68
	if (state.avgT == null) state.avgT = 68
	if (state.avgH == null) state.avgH = 50
	subscribeSelected()
}

def mainPage() 
{
	if (app.label == null)
	{
		app.updateLabel(app.name)
	}

	dynamicPage(name: "mainPage", uninstall: true, install: true) 
	{
		section(getFormat("title", " ${app.label}")) {}
		section{paragraph "<div style='color:#1A77C9'>Calculate a Rolling Average of a set of Illuminance (Lux) sensors.</div>"    
		input "vDevice", "capability.accelerationSensor", title: "Choose a Virtual Device to receive the Average.<i>(must support Illuminance)</i>"
		input "illumSensors", "capability.illuminanceMeasurement", title: "Choose Illuminance Sensors include in an Average", multiple: true
		input (name: "numberOption", type: "number", defaultValue: "10", range: "10..200", title: "Number of Samples to average.", description: "10 samples will be very responsive, while 200 samples is quite slow.", required: true)

	}
      section (title: "<b>Name/Rename</b>") {
      	label title: "This child app's Name (optional)", required: false
		input "debugOutput", "bool", title: "Enable Debug Logging?", required: false
	}
      display()
    } 
}


def display() {
    section{
	   paragraph getFormat("line")
	   paragraph "<div style='color:#1A77C9;text-align:center;font-weight:small;font-size:9px'>Developed by: C Steele<br/>Version Status: $state.status<br>Current Version: $state.version -  $state.Copyright</div>"
    }
}


def logsOff() {
    log.warn "debug logging disabled..."
    app?.updateSetting("debugOutput",[value:"false",type:"bool"])
}

def getFormat(type, myText=""){
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}


// Check Version   ***** with great thanks and acknowlegment to Cobra (CobraVmax) for his original code ****
def updateCheck()
{    
	
	def paramsUD = [uri: "https://hubitatcommunity.github.io/AverageThis/versions.json"]

 	asynchttpGet("updateCheckHandler", paramsUD) 
}

def updateCheckHandler(resp, data) {

	state.InternalName = "AverageThisChild"

	if (resp.getStatus() == 200 || resp.getStatus() == 207) {
		respUD = parseJson(resp.data)
		// log.warn " Version Checking - Response Data: $respUD"   // Troubleshooting Debug Code - Uncommenting this line should show the JSON response from your webserver 
		state.Copyright = "${thisCopyright}"
		def newVerRaw = (respUD.versions.Application.(state.InternalName))
		def newVer = (respUD.versions.Application.(state.InternalName).replaceAll("[.vV]", ""))
		def currentVer = version().replaceAll("[.vV]", "")   
		state.UpdateInfo = (respUD.versions.UpdateInfo.Application.(state.InternalName))
	
		if(newVer == "NLS")
		{
		      state.Status = "<b>** This driver is no longer supported by $respUD.author  **</b>"       
		      log.warn "** This driver is no longer supported by $respUD.author **"      
		}           
		else if(currentVer < newVer)
		{
		      state.Status = "<b>New Version Available (Version: $newVerRaw)</b>"
		      log.warn "** There is a newer version of this Application available  (Version: $newVerRaw) **"
		      log.warn "** $state.UpdateInfo **"
		} 
		else if(currentVer > newVer)
		{
		      state.Status = "<b>You are using a Test version of this Application (Version: $newVerRaw)</b>"
		}
		else
		{ 
		    state.Status = "Current"
		    if (descTextEnable) log.info "You are using the current version of this driver"
		}
	
	      if(state.Status == "Current")
	      {
	           state.UpdateInfo = "N/A"
	           sendEvent(name: "CodeUpdate", value: state.UpdateInfo)
	           sendEvent(name: "CodeStatus", value: state.Status)
	      }
	      else 
	      {
	           sendEvent(name: "CodeUpdate", value: state.UpdateInfo)
	           sendEvent(name: "CodeStatus", value: state.Status)
	      }
      }
      else
      {
           log.error "Something went wrong: CHECK THE JSON FILE AND IT'S URI"
      }
}

def getThisCopyright(){"&copy; 2019 C Steele "}
