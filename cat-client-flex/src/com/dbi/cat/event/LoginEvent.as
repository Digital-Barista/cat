package com.dbi.cat.event
{
	import flash.events.Event;
	
	public class LoginEvent extends Event
	{
		public static const LOGIN:String = "loginAttempt";
		public static const LOGOUT:String = "logout";
		
		public static const UPDATE_LAST_ACTIVITY:String = "updateLastActivityEvent";
		
		public var username:String;
		public var password:String;
		
		public function LoginEvent(type:String)
		{
			super(type, true, false);
		}

	}
}