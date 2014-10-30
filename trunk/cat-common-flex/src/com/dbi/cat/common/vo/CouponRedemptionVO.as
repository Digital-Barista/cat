package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.CouponRedemption")]
	public class CouponRedemptionVO 
	{
		
		public var couponExpirationDate:Date;
		public var couponExpirationDays:Number;
		public var offerUnavailableDate:Date;
		public var couponName:String;
		public var offerCode:String;
		public var responseDate:Date;
		public var responseDetail:String;
		public var responseType:String;
		public var actualMessage:String;
	
		public function CouponRedemptionVO()
		{
			super();
		}
	}
}