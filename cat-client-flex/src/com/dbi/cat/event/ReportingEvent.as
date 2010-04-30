package com.dbi.cat.event
{
	import com.dbi.cat.common.vo.NodeVO;
	
	import flash.events.Event;

	public class ReportingEvent extends Event
	{
		public static const LOAD_OUTGOING_MESSAGE_SUMMARIES:String = "loadOutgoingMessagesSummariesEvent";
		public static const LOAD_DASHBOARD	:String = "loadDashboardEvent";
		
			
		public function ReportingEvent(type:String)
		{
			super(type, true, false);
		}
	}
}