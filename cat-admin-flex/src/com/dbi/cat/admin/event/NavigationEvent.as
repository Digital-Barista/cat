package com.dbi.cat.admin.event
{
	
	import flash.events.Event;

	public class NavigationEvent extends Event
	{
		public static const LOGIN: String 	=	"loginNavEvent";
		public static const CLIENTS:String = "clientNavEvent";
		
		public function NavigationEvent(type:String)
		{
			super(type, true, false);
		}
		
	}
}