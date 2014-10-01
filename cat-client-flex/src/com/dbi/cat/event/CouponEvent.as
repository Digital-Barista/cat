package com.dbi.cat.event
{
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;

	public class CouponEvent extends Event
	{
		public static const REDEEM_COUPON:String = "redeemCouponEvent";
		public static const GET_COUPON_SUMMARY_BY_CLIENT:String = "getCouponSummaryByClientEvent";
		
		public var couponCode:String;
		public var clientIds:ArrayCollection;
		
		public function CouponEvent(type:String)
		{
			super(type, true, false);
		}
		
	}
}