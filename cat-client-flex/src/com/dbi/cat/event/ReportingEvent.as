package com.dbi.cat.event
{
	import com.dbi.cat.common.vo.NodeVO;
	
	import flash.events.Event;

	public class ReportingEvent extends Event
	{
		public static const OPEN_NODE_SUBSCRIBERS:String = "openNodeSubscribersEvent";
		public static const CLOSE_NODE_SUBSCRIBERS:String = "closeNodeSubscribersEvent";
		public static const LOAD_NODE_SUBSCRIBERS:String = "loadNodeSubscribersEvent";
		
		public var nodeUID:String;
			
		public function ReportingEvent(type:String)
		{
			super(type, true, false);
		}
	}
}