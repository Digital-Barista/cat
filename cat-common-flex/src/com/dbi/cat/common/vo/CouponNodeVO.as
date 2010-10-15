package com.dbi.cat.common.vo
{
	import flash.events.Event;
	import flash.events.EventDispatcher;
	
	import mx.collections.ArrayCollection;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.CouponNode")]
	public class CouponNodeVO extends NodeVO
	{
		public static const INFINITE_REDEMPTION_COUNT:Number = -1;
		public static const INFINITE_COUPONS_COUNT:Number = -1;
			
		public static const START_COUPON_CODE:String = "{";
		public static const END_COUPON_CODE:String = "}";
		
		public static const RANDOM_CODE_LENGTH:Number = 6;
		
		public function CouponNodeVO()
		{
			super();
		}
		
		public var couponId:Number;
		public var availableMessage:String = START_COUPON_CODE + END_COUPON_CODE;
		public var unavailableMessage:String;
		public var maxCoupons:Number = INFINITE_COUPONS_COUNT;
		public var maxRedemptions:Number = INFINITE_REDEMPTION_COUNT;
		public var unavailableDate:Date;
		public var couponCode:String;
		public var offerCode:String;
		public var expireDate:Date;
		public var expireDays:String;
		public var availableMessages:Object; // Map of messages indexed by EntryPointType.name
		public var unavailableMessages:Object; // Map of messages indexed by EntryPointType.name
		
		
		override public function get valid():Boolean
		{
			return availableMessage != null &&
				availableMessage.length > 0 &&
				expireDays != "";
		}
	}
}