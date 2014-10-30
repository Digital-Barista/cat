package com.dbi.cat.business
{
	import com.dbi.cat.event.NavigationEvent;
	
	import flash.events.IEventDispatcher;
	
	import mx.controls.Alert;
	import mx.core.Application;
	
	public class NavigationManager
	{
		
		public static const VIEW_LOGIN:Number = 0;
		public static const VIEW_RECOVER_PASSWORD:Number = 1;
		public static const VIEW_MAIN:Number = 2;
		
		public static const VIEW_MAIN_CAMPAIGNS:String = "Campaigns";
		public static const VIEW_MAIN_CONTACTS:String = "Contacts";
		public static const VIEW_MAIN_REPORTING:String = "Reporting";
		public static const VIEW_MAIN_USERS:String = "Users";
		public static const VIEW_MAIN_PROFILE:String = "Profile";
		public static const VIEW_MAIN_DASHBOARD:String = "Dashboard";
		
		public static const VIEW_RECOVER_PASSWORD_MAIN:String = "";
		public static const VIEW_RECOVER_PASSWORD_RESET:String = "resetPassword";
			
		[Bindable]
		public var appCurrentView:Number = VIEW_LOGIN;
		
		[Bindable]
		public var mainCurrentView:String = VIEW_MAIN_CAMPAIGNS;
				
		[Bindable]
		public var recoverPasswordView:String = VIEW_RECOVER_PASSWORD_MAIN;
		
		private var dispatcher:IEventDispatcher;
		
		public function NavigationManager(dispatcher:IEventDispatcher)
		{
			this.dispatcher = dispatcher;
		}

		public function navigate(e:NavigationEvent):void
		{
			switch (e.type)
			{
				case NavigationEvent.CONTACTS:
					appCurrentView = VIEW_MAIN;
					mainCurrentView = VIEW_MAIN_CONTACTS;
					break;
				case NavigationEvent.CAMPAIGNS:
					appCurrentView = VIEW_MAIN;
					mainCurrentView = VIEW_MAIN_CAMPAIGNS;
					break;
				case NavigationEvent.USERS:
					appCurrentView = VIEW_MAIN;
					mainCurrentView = VIEW_MAIN_USERS;
					break;
				case NavigationEvent.PROFILE:
					appCurrentView = VIEW_MAIN;
					mainCurrentView = VIEW_MAIN_PROFILE;
					break;
				case NavigationEvent.REPORTING:
					appCurrentView = VIEW_MAIN;
					mainCurrentView = VIEW_MAIN_REPORTING;
					break;
				case NavigationEvent.DASHBOARD:
					appCurrentView = VIEW_MAIN;
					mainCurrentView = VIEW_MAIN_DASHBOARD;
					break;
				case NavigationEvent.RECOVER_PASSWORD:
					appCurrentView = VIEW_RECOVER_PASSWORD;
					recoverPasswordView = VIEW_RECOVER_PASSWORD_MAIN;
					break;	
				case NavigationEvent.RECOVER_PASSWORD_RESET:
					appCurrentView = VIEW_RECOVER_PASSWORD;
					recoverPasswordView = VIEW_RECOVER_PASSWORD_RESET;
					break;
				case NavigationEvent.LOGIN:
					appCurrentView = VIEW_LOGIN;
					break;
			}
			
		}
	}
}