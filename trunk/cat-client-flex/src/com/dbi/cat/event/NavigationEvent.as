package com.dbi.cat.event
{
	
	import flash.events.Event;

	public class NavigationEvent extends Event
	{
		public static const NAV_CHANGED:String = "navChangedEvent";
		
		public static const LOGIN: String 	=	"loginNavEvent";
		public static const CAMPAIGNS:String = "campaignNavEvent";
		public static const COMMUNICATIONS:String = "communicationsNavEvent";
		public static const USERS:String = "usersNavEvent";
		public static const PROFILE:String = "profileNavEvent";
		public static const REPORTING:String = "reportingNavEvent";
		public static const CONTACTS:String = "contactsNavEvent";
		public static const DASHBOARD:String = "dashboardNavEvent";
		
		public static const CHANGE_PASSWORD:String = "changePasswordNavEvent";
		public static const RECOVER_PASSWORD:String = "recoverPasswordNavEvent";
		public static const RECOVER_PASSWORD_RESET:String = "recoverPasswordResetNavEvent";
		
		public function NavigationEvent(type:String, bubbles:Boolean=true, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
		
	}
}