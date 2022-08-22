/**
 *  Hubitat Import URL: https://raw.githubusercontent.com/HubitatCommunity/AverageThis/master/AverageThis.groovy
 *
 *  Average Illuminance
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
	name: "AverageThis",
	namespace: "csteele",
	author: "C Steele",
	description: "Average a set of Sensors into a Virtual Sensor.",
	category: "Averaging",
	iconUrl: "",
	iconX2Url: "",
	iconX3Url: "",
)


// Preference pages
preferences
{
	page(name: "mainPage")
}


def installed() {
	log.info "Installed with settings: ${settings}"
	initialize()
}


def updated() {
	log.info "Updated with settings: ${settings}"
	unschedule()
	unsubscribe()
	initialize()
}


def initialize() {
	version()
	log.info "There are ${childApps.size()} child Apps"
	childApps.each {child -> log.info "Child app: ${child.label}" }
}


def mainPage() {
	dynamicPage(name: "mainPage", uninstall: true, install: true)
	{
		section(getFormat("title", " ${app.label}")) {}
		section
		{
			paragraph "<div style='color:#1A77C9'>Calculate a Rolling Average of a set of Sensor values into a Virtual Sensor.</div>"    
			paragraph title: "<AverageThis",
			"<b>This parent app is a container for all:</b><br> AverageThis child apps"
		}
		section 
		{
			app(name: "AverageThisChild", appName: "AverageThisChild", namespace: "csteele", title: "<b>New AverageThis child</b>", multiple: true)
			app(name: "AverageThisPower", appName: "AverageThisPower", namespace: "csteele", title: "<b>New AverageThisPower child</b>", multiple: true)
		}    
		section (title: "<b>Name/Rename</b>") 
		{
			label title: "Enter a name for this parent app (optional)", required: false
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


def getFormat(type, myText=""){
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}


def getThisCopyright(){"&copy; 2020 C Steele "}
