package com.dbi.cat.common.vo
{
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.util.CouponRedemptionMessage")]
	public class CodedMessageVO
	{
		
		public static const SUCCESS:Number=0;
		
		public static const OVER_MAX_CODE:Number=1;
		public static const EXPIRED_CODE:Number=2;
		public static const NOT_FOUND_CODE:Number=3;
		public static const NOT_ALLOWED_CODE:Number=4;
		
		public static const UNKNOWN_ERROR:Number=5;
	
		public var code:Number;
		public var message:String;
		public var detailedMessage:String;
	
		public function CodedMessageVO()
		{
		}

	}
}