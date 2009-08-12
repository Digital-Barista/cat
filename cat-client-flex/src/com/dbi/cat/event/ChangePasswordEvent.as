package com.dbi.cat.event
{
	import flash.events.Event;

	public class ChangePasswordEvent extends Event
	{
		public static const RECOVER_PASSWORD_INITIATE:String  = "recoverPasswordInitiateEvent";
		public static const RECOVER_PASSWORD_RESET:String = "recoverPasswordResetEvent";
		public static const CHANGE_PASSWORD:String = "changePasswordEvent";
		
		public var userId:String;
		public var emailAddress:String;
		public var recoverPasswordCode:String;
		public var oldPassword:String;
		public var password:String;
		public var confirmPassword:String;
		
		public function ChangePasswordEvent(type:String, bubbles:Boolean=true, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
		
		public override function clone():Event
		{
			return new ChangePasswordEvent(super.type, super.bubbles, super.cancelable);
		}
		
	}
}