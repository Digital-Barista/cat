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
		
		public static const VIEW_MAIN_CAMPAIGNS:Number = 0;
		public static const VIEW_MAIN_USERS:Number = 1;
		public static const VIEW_MAIN_PROFILE:Number = 2;
		public static const VIEW_MAIN_CHANGE_PASSWORD:Number = 3;
		public static const VIEW_MAIN_EDIT_USER:Number = 4;
		
		public static const VIEW_RECOVER_PASSWORD_MAIN:String = "";
		public static const VIEW_RECOVER_PASSWORD_RESET:String = "resetPassword";
			
		[Bindable]
		public var appCurrentView:Number = VIEW_LOGIN;
		
		[Bindable]
		public var mainCurrentView:Number = VIEW_MAIN_CAMPAIGNS;
				
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
				case NavigationEvent.RECOVER_PASSWORD:
					appCurrentView = VIEW_RECOVER_PASSWORD;
					recoverPasswordView = VIEW_RECOVER_PASSWORD_MAIN;
					break;	
				case NavigationEvent.RECOVER_PASSWORD_RESET:
					appCurrentView = VIEW_RECOVER_PASSWORD;
					recoverPasswordView = VIEW_RECOVER_PASSWORD_RESET;
					break;
				case NavigationEvent.CHANGE_PASSWORD:
					appCurrentView = VIEW_MAIN;
					mainCurrentView = VIEW_MAIN_CHANGE_PASSWORD;
					break;
				case NavigationEvent.LOGIN:
					appCurrentView = VIEW_LOGIN;
					break;
			}
			
		}
	}
}