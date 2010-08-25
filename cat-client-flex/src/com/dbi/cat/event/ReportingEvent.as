package com.dbi.cat.event
{
	import com.dbi.cat.common.vo.NodeVO;
	
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;

	public class ReportingEvent extends Event
	{
		public static const LOAD_OUTGOING_MESSAGE_SUMMARIES:String = "loadOutgoingMessagesSummariesEvent";
		public static const LOAD_DASHBOARD:String = "loadDashboardEvent";
		public static const LOAD_TAG_SUMMARIES:String = "loadTagSummariesEvent";
		
		public var clientIDs:ArrayCollection;
			
		public function ReportingEvent(type:String)
		{
			super(type, true, false);
		}
	}
}