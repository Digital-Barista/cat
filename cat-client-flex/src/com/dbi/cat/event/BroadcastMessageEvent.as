package com.dbi.cat.event
{
	import com.dbi.cat.common.constants.EntryPointType;
	import com.dbi.cat.common.vo.CampaignVO;
	import com.dbi.cat.common.vo.CouponNodeVO;
	import com.dbi.cat.common.vo.MessageVO;
	
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;

	public class BroadcastMessageEvent extends Event
	{
    public static const OPEN_CONFIRM_BROADCAST_MESSAGE:String = "openConfirmBroadcastMessageEvent";
    public static const CLOSE_CONFIRM_BROADCAST_MESSAGE:String = "closeConfirmBroadcastMessageEvent";
      
		public static const SEND_BROADCAST_MESSAGE:String = "sendBroadcastMessageEvent";
    public static const SEND_BROADCAST_COUPON_MESSAGE:String = "sendBroadcastCouponMessageEvent";
		
    public var clientID:Number;
		public var campaign:CampaignVO;
		public var message:MessageVO;
    public var coupon:CouponNodeVO;
		public var entryDatas:ArrayCollection;
			
		public function BroadcastMessageEvent(type:String)
		{
			super(type, true, false);
		}
	}
}