package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.BroadcastInfo")]
	public class BroadcastInfoVO
	{
		public function BroadcastInfoVO()
		{
			super();
		}
    
    public var subscriberCount:Number;
    public var title:String;
    public var message:String;
    public var couponRedemptionCount:Number;
    public var isCoupon:Boolean = false;
    public var couponId:Number;
    public var entryPointUID:String;
	}
}