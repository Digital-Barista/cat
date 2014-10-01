package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.reporting.DashboardData")]
	public class DashboardDataVO
	{
		public function DashboardDataVO()
		{
			super();
		}
		
		public var clientCount:String;
		public var campaignCount:String;
		public var subscriberCount:String;
		public var messageCreditInfos:ArrayCollection;
		
		public var couponsSent:String;
		public var couponsRedeemed:String;
		public var contactCounts:ArrayCollection;
		public var messagesSent:ArrayCollection;
		public var messagesReceived:ArrayCollection;
		public var endPointSubscriberCounts:ArrayCollection;
	}
}