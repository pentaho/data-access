/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
