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
 public static String version()      {  return "v1.1"  }

definition (
	name: "AverageThis",
	namespace: "csteele",
	author: "C Steele",
	description: "Average a set of Illuminance (Lux) sensors.",
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
	schedule("0 0 8 ? * FRI *", updateCheck)
	unsubscribe()
	updateCheck()
	initialize()
}


def initialize() {
	log.info "There are ${childApps.size()} child Apps"
	childApps.each {child -> log.info "Child app: ${child.label}" }
//    state.remove("version")
//    state.remove("Version")
//    state.remove("Copyright")
}


def mainPage() {
	dynamicPage(name: "mainPage", uninstall: true, install: true)
	{
		section(getFormat("title", " ${app.label}")) {}
		section
		{
			paragraph "<div style='color:#1A77C9'>Calculate a Rolling Average of a set of Omni sensors.</div>"    
			paragraph title: "<AverageThis",
			"<b>This parent app is a container for all:</b><br> AverageThis child apps"
		}
		section 
		{
			app(name: "AverageThisChild", appName: "AverageThisChild", namespace: "csteele", title: "New AverageThis child", multiple: true)
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


// Check Version   ***** with great thanks and acknowlegment to Cobra (CobraVmax) for his original code ****
def updateCheck()
{    
	
     def paramsUD = [uri: "https://hubitatcommunity.github.io/AverageThis/versions.json"]
	
 	asynchttpGet("updateCheckHandler", paramsUD) 
}

def updateCheckHandler(resp, data) {

	state.InternalName = "AverageThis"

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
		    log.info "You are using the current version of this Application"
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
