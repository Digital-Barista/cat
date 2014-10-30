package com.dbi.event
{
	import flash.events.Event;
	
	public class CustomMessageEvent extends Event
	{
		public static const EVENT_CUSTOM_MESSAGE:String = "customMessage";
		
		public function CustomMessageEvent()
		{
			super(EVENT_CUSTOM_MESSAGE);
		}

		public var ButtonText:String;
		public var Message:String;
	}
}