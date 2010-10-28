package com.dbi.cat.event
{
	import com.dbi.cat.common.constants.EntryPointType;
	import com.dbi.cat.common.vo.CampaignVO;
	import com.dbi.cat.common.vo.NodeVO;
	
	import flash.events.Event;

	public class CampaignMessageEvent extends Event
	{
		public static const OPEN_MESSAGE_PREVIEW:String = "openMessagePreviewEvent";
		public static const CLOSE_MESSAGE_PREVIEW:String = "closeMessagePreviewEvent";
		public static const GET_MESSAGE_PART:String = "getMessagePartEvent";
		public static const GET_MESSAGE_NODE_PARTS:String = "getMessageNodePartsEvent";
		public static const GET_AVAILABLE_COUPON_NODE_PARTS:String = "getAvailableCouponNodePartsEvent";
		public static const GET_UNAVAILABLE_COUPON_NODE_PARTS:String = "getUnavailableCouponNodePartsEvent";
		
		public var campaign:CampaignVO;
		public var message:String;
		public var node:NodeVO;
		public var entryType:EntryPointType;
		public var title:String;
			
		public function CampaignMessageEvent(type:String)
		{
			super(type, true, false);
		}
	}
}