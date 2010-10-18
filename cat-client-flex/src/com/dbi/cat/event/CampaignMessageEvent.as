package com.dbi.cat.event
{
	import com.dbi.cat.common.vo.CampaignVO;
	
	import flash.events.Event;

	public class CampaignMessageEvent extends Event
	{
		public static const GET_MESSAGE_PARTS:String = "getMessagePartsEvent";
		public static const OPEN_MESSAGE_PREVIEW:String = "openMessagePreviewEvent";
		public static const CLOSE_MESSAGE_PREVIEW:String = "closeMessagePreviewEvent";
		
		public var campaign:CampaignVO;
		public var message:String;
			
		public function CampaignMessageEvent(type:String)
		{
			super(type, true, false);
		}
	}
}