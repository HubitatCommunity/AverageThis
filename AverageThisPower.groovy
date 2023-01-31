/**
 *  Hubitat Import URL: https://raw.githubusercontent.com/HubitatCommunity/AverageThis/master/AverageThis.groovy
 *
 *  Average Illuminance, Temperature, Relative Humidity
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
	public static String version()      {  return "v1.2"  }

definition (
	name: "AverageThisPower",
	namespace: "csteele",
	author: "C Steele",
	description: "Child: Average a set of Power sensors.",
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
	if (debugOutput) log.debug "subscribeSelected: $theseSensors"
	if (theseSensors?.size()) 
	{
	    subscribe(theseSensors, "power", powerHandler)
	    subscribe(theseSensors, "voltage", voltageHandler)
	    subscribe(theseSensors, "current", currentHandler)
	    subscribe(theseSensors, "level", levelHandler)
	    subscribe(theseSensors, "energy", energyHandler)
	}
}

// setCurrent, setEnergy, setLevel, setPower, setVoltage

def powerHandler(evt) {
	def NSample = numberOption as Integer
	if (debugOutput) log.debug "powerHandler: $evt.device, $evt.value, ($state.avgP)"
	/* exponentially weighted average
	    avg -= avg / N;
    	    avg += new_sample / N;	
	 when N=1 avg = the latest value. When N > 100 is approximately the number of previous values to average
	 */	 
	def float avP = state.avgP
	avP -= avP / NSample
	avP += Float.parseFloat(evt.value) / NSample
	state.avgP = avP
	
	if(vDevice.supportedCommands.find{it.toString() == "setPower"}) { settings.vDevice.setPower("${state.avgP.round(1)}"); sendEvent(name: "power", value: state.avgP, unit: "W", displayed: true)  }
	else { log.warn "Is Incorrect vDevice - no Power" }
}

def voltageHandler(evt) {
	def NSample = numberOption as Integer
	if (debugOutput) log.debug "voltageHandler: $evt.device, $evt.value, ($state.avgV)"
	def float avV = state.avgV
	avV -= avV / NSample
	avV += Float.parseFloat(evt.value) / NSample
	state.avgV = avV
	
	if(vDevice.supportedCommands.find{it.toString() == "setVoltage"}) { settings.vDevice.setVoltage("${state.avgV.round(1)}"); sendEvent(name: "voltage", value: state.avgV, unit: "V", displayed: true)  }
	else { log.warn "Is Incorrect vDevice - no Voltage" }
}

def currentHandler(evt) {
	def NSample = numberOption as Integer
	if (debugOutput) log.debug "currentHandler: $evt.device, $evt.value, ($state.avgC)"
	def float avC = state.avgC
	avC -= avC / NSample
	avC += Float.parseFloat(evt.value) / NSample
	state.avgC = avC
	
	if(vDevice.supportedCommands.find{it.toString() == "setCurrent"}) { settings.vDevice.current("${state.avgC.round(1)}"); sendEvent(name: "current", value: state.avgC, unit: "A", displayed: true)  }
	else { log.warn "Is Incorrect vDevice - no Current" }
}

def energyHandler(evt) {
	def NSample = numberOption as Integer
	if (debugOutput) log.debug "energyHandler: $evt.device, $evt.value, ($state.avgE)"
	def float avE = state.avgE
	avE -= avE / NSample
	avE += Float.parseFloat(evt.value) / NSample
	state.avgE = avE
	
	if(vDevice.supportedCommands.find{it.toString() == "setEnergy"}) { settings.vDevice.setVoltage("${state.avgE.round(1)}"); sendEvent(name: "energy", value: state.avgE, displayed: true)  }
	else { log.warn "Is Incorrect vDevice - no Energy" }
}

def levelHandler(evt) {
	def NSample = numberOption as Integer
	if (debugOutput) log.debug "levelHandler: $evt.device, $evt.value, ($state.avgL)"
	def float avL = state.avgL
	avL -= avL / NSample
	avL += Float.parseFloat(evt.value) / NSample
	state.avgL = avL
	
	if(vDevice.supportedCommands.find{it.toString() == "setLevel"}) { settings.vDevice.current("${state.avgL.round(1)}"); sendEvent(name: "level", value: state.avgL, displayed: true)  }
	else { log.warn "Is Incorrect vDevice - mo Level" }
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
	initialize()
}

def initialize() {
	version()
	// an inital value of 0 will take a long time to average out, thus avg is initialized to an arbitrary average
	if (state.avgP == null) state.avgP = 20
	if (state.avgV == null) state.avgV = 100
	if (state.avgC == null) state.avgC = 1
	if (state.avgE == null) state.avgE = 100
	if (state.avgL == null) state.avgL = 0
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
		section{paragraph "<div style='color:#1A77C9'>Calculate a Rolling Average of a set of Power sensors.</div>"    
		input "vDevice", "capability.powerMeter", title: "Choose a Virtual Device to receive the Average.<i>(must support PowerMeter)</i>"
		input "theseSensors", "capability.powerMeter", title: "Choose Power Sensors to include in an Average", multiple: true
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
	   paragraph "<div style='color:#1A77C9;text-align:center;font-weight:small;font-size:9px'>Developed by: C Steele<br/>Version Status: $state.status<br>Current Version: ${version()} -  ${thisCopyright}</div>"
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

def getThisCopyright(){"&copy; 2020 C Steele "}
