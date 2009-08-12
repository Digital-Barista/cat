package com.dbi.cat.event
{
	import com.dbi.cat.common.vo.CampaignVO;
	
	import flash.events.Event;

	public class CampaignTestEvent extends Event
	{
		public static const START_TEST:String = "startTestEvent";
		public static const END_TEST_CAMPAIGN:String = "endTestCampaignEvent";
		public static const SEND_RESPONSE:String = "sendResponseEvent";
		public static const FOLLOW_NEXT_TIME_CONNECTOR:String = "followNextTimeConnectorEvent";
	
		public var startDate:Date;
		public var campaign:CampaignVO;
		
		public var response:String;
			
		public function CampaignTestEvent(type:String)
		{
			super(type, true, false);
		}
	}
}