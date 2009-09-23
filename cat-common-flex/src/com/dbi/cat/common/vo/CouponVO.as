package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.Coupon")]
	public class CouponVO 
	{
		
		public var campaignName:String;
		public var couponName:String;
		public var maxCouponCount:Number;
		public var maxRedemptions:Number;
		public var unavailableDate:Date;
		public var issuedCouponCount:Number;
		public var totalRedemptionCount:Number;
		public var averageRedemptionTime:Number;
	
		public function CouponVO()
		{
			super();
		}
	}
}