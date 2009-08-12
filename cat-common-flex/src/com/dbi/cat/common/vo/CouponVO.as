package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.CouponNode")]
	public class CouponVO extends NodeVO
	{
		public static const INFINITE_REDEMPTION_COUNT:Number = -1;
			
		public function CouponVO()
		{
			super();
		}
		
		public var couponId:Number;
		public var availableMessage:String;
		public var unavailableMessage:String;
		public var maxRedemptions:Number = INFINITE_REDEMPTION_COUNT;
		public var unavailableDate:Date;
		
		
		override public function get valid():Boolean
		{
			return availableMessage != null &&
				availableMessage.length > 0;
		}
	}
}