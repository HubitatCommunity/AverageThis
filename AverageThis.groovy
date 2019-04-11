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


// App Version   ***** with great thanks and acknowlegment to Cobra (CobraVmax) for his original version checking code ********
def setVersion() {
	state.version = "1.0"
	state.InternalName = "AverageThis"
	state.Type = "Application"
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
    version()
    section{
	   paragraph getFormat("line")
	   paragraph "<div style='color:#1A77C9;text-align:center;font-weight:small;font-size:9px'>Developed by: C Steele<br/>Version Status: $state.status<br>Current Version: $state.version -  $state.Copyright</div>"
    }
}


def getFormat(type, myText=""){
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}


// Check Version   ***** with great thanks and acknowlegment to Cobra (CobraVmax) for his original code **************
def version() {
    updatecheck()
    schedule("0 0 8 ? * FRI *", updatecheck)
}


def updatecheck() {
    setVersion()
     def paramsUD = [uri: "https://hubitatcommunity.github.io/AverageThis/versions.json"]
    try {
     httpGet(paramsUD) { respUD ->
         //  log.warn " Version Checking - Response Data: ${respUD.data}"   // Troubleshooting Debug Code 
       	def copyrightRead = (respUD.data.copyright)
       	state.Copyright = copyrightRead
            def commentRead = (respUD.data.Comment)
       	state.Comment = commentRead
            def newVerRaw = (respUD.data.versions.Application.(state.InternalName))
            state.newver = newVerRaw
            def newVer = (respUD.data.versions.Application.(state.InternalName).replace(".", ""))
       	def currentVer = state.version.replace(".", "")
      	state.UpdateInfo = (respUD.data.versions.UpdateInfo.Application.(state.InternalName))
            state.author = (respUD.data.author)
           
		if (newVer == "NLS"){
                state.status = "<b>** This app is no longer supported by $state.author  **</b>"  
                log.warn "** This app is no longer supported by $state.author **" 
      	}           
		else if (currentVer < newVer) {
        	    state.status = "<b>New Version Available ($newVerRaw)</b>"
        	    log.warn "** There is a newer version of this app available  (Version: $newVerRaw) **"
        	    log.warn " Update: $state.UpdateInfo "
                state.newBtn = state.status
                state.updateMsg = "There is a new version of '$state.ExternalName' available (Version: $newVerRaw)"
       	} 
            else if (currentVer > newVer) {
           	    state.status = "<b>You are using a Test version of this Driver (Version: $newVerRaw)</b>"
            }
		else { 
      	    state.status = "Current"
       	}
     }
    } 
    catch (e) {
        	log.error "Something went wrong: CHECK THE JSON FILE AND IT'S URI -  $e"
    }
    if (state.status != "Current") {
		state.newBtn = state.status
    }
    else {
        state.newBtn = "No Update Available"
    }
}
