/* EventTarget from N Zakas Professsional Javascript book */
function EventTarget(){
    this.handlers = {};    
}

EventTarget.prototype = {
    constructor: EventTarget,

    listen: function(type, handler){
        if (typeof this.handlers[type] == "undefined"){
            this.handlers[type] = [];
        }

        this.handlers[type].push(handler);
    },
    
    notify: function(event){
        if (!event.target){
            event.target = this;
        }
        if (this.handlers[event.type] instanceof Array){
            var handlers = this.handlers[event.type];
            for (var i=0, len=handlers.length; i < len; i++){
                handlers[i](event);
            }
        }            
    },

    ignore: function(type, handler){
        if (this.handlers[type] instanceof Array){
            var handlers = this.handlers[type];
            for (var i=0, len=handlers.length; i < len; i++){
                if (handlers[i] === handler){
                    break;
                }
            }
            
            handlers.splice(i, 1);
        }            
    }
};


//= require "oop.js"

/* define the pentaho namespace if it is not already defined. */
var pentaho = pentaho || {};

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