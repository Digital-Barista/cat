package com.dbi.cat.business
{
	import com.dbi.cat.event.LoginEvent;
	import com.dbi.controls.CustomMessage;
	
	import flash.events.IEventDispatcher;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IViewCursor;
	import mx.core.Application;
	import mx.rpc.Fault;
	
	[Bindable]
	public class AppManager
	{
		
		public var currentVersion:String;
		public var workspaceStatusMessage:String;
		public var generalStatusMessage:String;
		
		private var workspaceStatusMessageList:ArrayCollection;
		private var generalStatusMessageList:ArrayCollection;
		
		private var dispatcher:IEventDispatcher;
		
		public function AppManager(dispatcher:IEventDispatcher)
		{
			this.dispatcher = dispatcher;
			workspaceStatusMessageList = new ArrayCollection();
			generalStatusMessageList = new ArrayCollection();
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
		 * Adds a status message to the queue that will display everywhere
		 * in the application
		 * 
		 * @param message Message string to add
		 */
		public function addGeneralStatusMessage(message:String):void
		{
			generalStatusMessageList.addItem(message);
			updateGeneralStatusMessage();
		}
		
		/**
		 * Removes a single status message that has been added with 
		 * matching text
		 * 
		 * @param message Status message to remove
		 */
		public function removeGeneralStatusMessage(message:String):void
		{
			var cur:IViewCursor = generalStatusMessageList.createCursor();
			while (cur.current != null)
			{
				if (cur.current == message)
				{
					cur.remove();
					break;
				}
				cur.moveNext();
			}
			updateGeneralStatusMessage();
		}
		private function updateGeneralStatusMessage():void
		{
			if (generalStatusMessageList.length > 0)
				generalStatusMessage = generalStatusMessageList[generalStatusMessageList.length - 1] + "...";
			else
				generalStatusMessage = "";
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
		 	else if (fault.rootCause != null &&
		 		fault.rootCause.cause != null &&
		 		fault.rootCause.cause.message != null &&
		 		fault.rootCause.message != null &&
		 		fault.rootCause.message.indexOf("FlexException") > -1)
		 	{
		 		CustomMessage.show(fault.rootCause.cause.message);
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