package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.reporting.MessageCreditInfo")]
	public class MessageCreditInfoVO
	{
		public function MessageCreditInfoVO()
		{
			super();
		}
		
		public var clientName:String;
		public var network:String;
		public var credits:String;
		public var usedThisMonth:String;
		public var usedTotal:String;
	}
}