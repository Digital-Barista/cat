package com.dbi.cat.common.vo
{
	[Bindable]
	public class CouponReportVO
	{
		public var campaignName:String;
		public var coupon:String;
		public var maxCoupons:Number;
		public var maxRedepmtions:Number;
		public var unavailableDate:Date;
		public var sent:Number;
		public var redeemed:Number;
		public var averageRedemptionTime:String;
		
		public function CouponReportVO()
		{
		}

	}
}