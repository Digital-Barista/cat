package com.dbi.cat.business
{
	import com.dbi.cat.event.LoginEvent;
	import com.dbi.controls.CustomMessage;
	
	import flash.events.IEventDispatcher;
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IViewCursor;
	import mx.core.Application;
	import mx.rpc.Fault;
	
	[Bindable]
	public class AppManager
	{
		
		public var currentVersion:String;
		public var workspaceStatusMessage:String;
		
		private var workspaceStatusMessageList:ArrayCollection;
		private var dispatcher:IEventDispatcher;
		
		public function AppManager(dispatcher:IEventDispatcher)
		{
			this.dispatcher = dispatcher;
			workspaceStatusMessageList = new ArrayCollection();
		}
		
		/**
		 * Grabs any external parameters passed into the flash movie
		 */
		public function loadExternalParameters():void
		{
			var params:Object = Application.application.parameters;
			currentVersion = params.cat_version;
		}
		
		/**
		 * Adds a status message about activity on the workspace to a
		 * queue of messages
		 * 
		 * @param message Message string to add
		 */
		public function addWorkspaceStatusMessage(message:String):void
		{
			workspaceStatusMessageList.addItem(message);
			updateCurrentWorkspaceStatusMessage();
		}
		
		/**
		 * Removes a single status message that has been added with 
		 * matching text
		 * 
		 * @param message Status message to remove
		 */
		public function removeWorkspaceStatusMessage(message:String):void
		{
			var cur:IViewCursor = workspaceStatusMessageList.createCursor();
			while (cur.current != null)
			{
				if (cur.current == message)
				{
					cur.remove();
					break;
				}
				cur.moveNext();
			}
			updateCurrentWorkspaceStatusMessage();
		}
		private function updateCurrentWorkspaceStatusMessage():void
		{
			if (workspaceStatusMessageList.length > 0)
				workspaceStatusMessage = workspaceStatusMessageList[workspaceStatusMessageList.length - 1] + "...";
			else
				workspaceStatusMessage = "";
		}
		
		/**
		 * Register class aliases so ObjectUtil.copy() will preserve
		 * the object type
		 */
		public function registerClasses():void
		{
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