package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.Subscriber")]
	public class SubscriberVO
	{
		public function SubscriberVO()
		{
			super();
		}
		
		public var subscriberId:Number;
		public var entryPointType:String;
		public var address:String;
		
	}
}