package com.dbi.cat.event
{
	import com.dbi.cat.common.constants.EntryPointType;
	import com.dbi.cat.common.vo.CampaignVO;
	import com.dbi.cat.common.vo.MessageVO;
	
	import flash.events.Event;

	public class BroadcastMessageEvent extends Event
	{
		public static const SEND_BROADCAST_MESSAGE:String = "sendBroadcastMessageEvent";
    public static const SEND_BROADCAST_COUPON_MESSAGE:String = "sendBroadcastCouponMessageEvent";
		
		public var campaign:CampaignVO;
		public var message:MessageVO;
    public var coupon:CouponNodeVO;
		public var entryType:EntryPointType;
			
		public function BroadcastMessageEvent(type:String)
		{
			super(type, true, false);
		}
	}
}