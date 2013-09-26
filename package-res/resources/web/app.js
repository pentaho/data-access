/*!
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

/*
pentaho.app concept is taken from Nicholas Zakas, Scalable JavaScript Architecture
*/

pentaho.app = function(app){
	EventTarget.call(this); //call parent object
	this.moduleData = [];
}

inheritPrototype(pentaho.app, EventTarget); //borrow the EventTarget's methods

pentaho.app.prototype.init = function(modArray) {

	var module = {};
	//register first then start
	for (var i=0,j=modArray.length;i<j;i++) {
		module = modArray[i];
		this.register(module);
		this.start(module);
//		console.log('started module: ' + module.name);
	}
	
}

pentaho.app.prototype.register = function(module){
	this.moduleData[module.name] = {name: module.name, creator: module.objectClass, instance: null}
}  //end register

pentaho.app.prototype.start = function(module){
	this.moduleData[module.name].instance = new this.moduleData[module.name].creator(this);
	this.moduleData[module.name].instance.init(module.element||{});
}  //end start

pentaho.app.prototype.stop = function(moduleId){
	var data = this.moduleData[moduleId];
	if (data.instance) {
		data.instance.destroy();
		data.instance = null;
	}
}

pentaho.app.prototype.startAll = function(){
	for (var moduleId in this.moduleData){
		if (this.moduleData.hasOwnProperty(moduleId)){
			this.start(moduleId);
		}
	}
}
pentaho.app.prototype.stopAll = function(){
	for (var moduleId in this.moduleData){
		if (this.moduleData.hasOwnProperty(moduleId)){
			this.stop(moduleId);
		}
	}
}
pentaho.app.prototype.getModuleData = function() {
	var data = [], mod;
	// return all if no args supplied
	if (arguments.length > 0) {
		//if (typeof arguments[0] == 'object') {
		//}
		for (mod in this.moduleData) {
			data[data.length] = this.moduleData[mod].instance;
		}
		return data;
	} else {
		for (mod in this.moduleData) {
			data[data.length] = this.moduleData[mod].instance;
		}
		return data;		
	}
}