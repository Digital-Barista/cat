package com.dbi.cat.admin.event
{
	import flash.events.Event;
	
	public class LoadDataEvent extends Event
	{
		public static const INITIALIZE_DATA:String = "initializeDataEvent";
		public static const DATA_LOADED:String = "dataLoaded";
		
		public function LoadDataEvent(type:String)
		{
			super(type, true, false);
		}

	}
}