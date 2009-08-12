package com.dbi.cat.business
{
	import com.dbi.cat.event.LoginEvent;
	import com.dbi.cat.event.WorkspaceEvent;
	import com.dbi.controls.CustomMessage;
	
	import flash.events.IEventDispatcher;
	import flash.events.KeyboardEvent;
	import flash.ui.Keyboard;
	
	import mx.core.Application;
	import mx.rpc.Fault;
	
	public class AppManager
	{
		[Bindable]
		public var currentVersion:String;
		
		private var dispatcher:IEventDispatcher;
		
		public function AppManager(dispatcher:IEventDispatcher)
		{
			this.dispatcher = dispatcher;
		}

		public function loadExternalParameters():void
		{
			var params:Object = Application.application.parameters;
			currentVersion = params.cat_version;
		}
		
		/**
		 * Register class aliases so ObjectUtil.copy() will preserve
		 * the object type
		 */
		public function registerClasses():void
		{
		}
		public function setupGlobalEvents():void
		{
			Application.application.addEventListener(KeyboardEvent.KEY_DOWN, onKeyDown);
		}
		
		private function onKeyDown(e:KeyboardEvent):void
		{
			if (e.target.hasOwnProperty("id") &&
				e.target.id == "WorkspaceTabs" &&
				e.keyCode == Keyboard.DELETE)
			{
				dispatcher.dispatchEvent(new WorkspaceEvent(WorkspaceEvent.REMOVE_SELECTED_WORKSPACE_ITEMS));
			}
		}
		
		/**
		 * Generic method for handling service call faults that just reports
		 * the fault info to the user
		 */
		 public function showFault(fault:Fault):void
		 {
		 	if (fault.faultCode == "Client.Authentication")
		 	{
		 		CustomMessage.show("Your session has ended.  Please login again");
		 		dispatcher.dispatchEvent(new LoginEvent(LoginEvent.LOGOUT));
		 	}
		 	else
		 	{
		 		CustomMessage.show(fault.toString());
		 	}
		 }
		 
		 /**
		 * Hides a fault from the user
		 */
		 public function hideFault(fault:Fault):void
		 {
		 	
		 }
	}
}