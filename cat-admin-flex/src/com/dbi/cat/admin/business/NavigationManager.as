package com.dbi.cat.admin.business
{
	import com.dbi.cat.admin.event.NavigationEvent;
	
	import flash.events.IEventDispatcher;
	
	public class NavigationManager
	{
		
		public static const VIEW_LOGIN:Number = 0;
		public static const VIEW_MAIN:Number = 1;
		
		public static const VIEW_MAIN_CLIENTS:Number = 0;
			
		[Bindable]
		public var appCurrentView:Number = VIEW_LOGIN;
		
		[Bindable]
		public var mainCurrentView:Number = VIEW_MAIN_CLIENTS;
		
		private var dispatcher:IEventDispatcher;
		
		public function NavigationManager(dispatcher:IEventDispatcher)
		{
			this.dispatcher = dispatcher;
		}

		public function navigate(type:String):void
		{
			switch (type)
			{
				case NavigationEvent.LOGIN:
					appCurrentView = VIEW_LOGIN;
					mainCurrentView = VIEW_MAIN_CLIENTS;
					break;
				case NavigationEvent.CLIENTS:
					appCurrentView = VIEW_MAIN;
					mainCurrentView = VIEW_MAIN_CLIENTS;
					break;
			}
			
		}
	}
}