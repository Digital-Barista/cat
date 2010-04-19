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
		public var contactCount:String;
		public var subscriberCount:String;
		public var messageCreditInfos:ArrayCollection;
	}
}