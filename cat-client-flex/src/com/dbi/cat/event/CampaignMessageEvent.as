package com.dbi.cat.event
{
	import com.dbi.cat.common.vo.CampaignVO;
	
	import flash.events.Event;

	public class CampaignMessageEvent extends Event
	{
		public static const GET_MESSAGE_PARTS:String = "getMessagePartsEvent";
		
		public var campaign:CampaignVO;
		public var message;
			
		public function CampaignMessageEvent(type:String)
		{
			super(type, true, false);
		}
	}
}