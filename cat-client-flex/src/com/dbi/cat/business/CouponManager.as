package com.dbi.cat.business
{
	import com.dbi.cat.common.vo.CodedMessageVO;
	
	import mx.collections.ArrayCollection;
	
	[Bindable]
	public class CouponManager
	{
		public var coupons:ArrayCollection;
		public var redemptionMessage:String;
		public var redemptionClass:String = "successMessage";
		
		public function CouponManager()
		{
		}

		public function loadCoupon(coupons:ArrayCollection):void
		{
			this.coupons = coupons;
		}
		
		public function clearRedemptionMessage():void
		{
			redemptionMessage = "";
		}
		
		public function showCouponRedemption(message:CodedMessageVO):void
		{
			redemptionMessage = message.message;
			
			if (message.code == CodedMessageVO.SUCCESS)
				redemptionClass = "successMessage";
			else
				redemptionClass = "failMessage";
		}
	}
}