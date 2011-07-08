package com.dbi.cat.event
{
	import com.dbi.cat.common.constants.EntryPointType;
	import com.dbi.cat.common.vo.CampaignVO;
	import com.dbi.cat.common.vo.CouponNodeVO;
	import com.dbi.cat.common.vo.MessageVO;
	import com.dbi.cat.common.vo.criteria.ContactSearchCriteriaVO;
	
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;

	public class BroadcastMessageEvent extends Event
	{
    public static const LIST_BROADCAST_MESSAGES:String = "listBroadcastMessagesEvent";
    public static const OPEN_CONFIRM_BROADCAST_MESSAGE:String = "openConfirmBroadcastMessageEvent";
    public static const CLOSE_CONFIRM_BROADCAST_MESSAGE:String = "closeConfirmBroadcastMessageEvent";
      
		public static const SEND_BROADCAST_MESSAGE:String = "sendBroadcastMessageEvent";
    public static const SEND_BROADCAST_COUPON_MESSAGE:String = "sendBroadcastCouponMessageEvent";
		
    public var clientID:Number;
    public var clientIDs:ArrayCollection;
		public var campaign:CampaignVO;
		public var message:MessageVO;
    public var coupon:CouponNodeVO;
		public var entryDatas:ArrayCollection;
    public var search:ContactSearchCriteriaVO;
			
		public function BroadcastMessageEvent(type:String)
		{
			super(type, true, false);
		}
	}
}