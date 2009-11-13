package com.dbi.cat.common.vo
{
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.Keyword")]
	public class KeywordVO
	{
		
		public var entryPointId:String;
		public var clientId:String;
		public var keyword:String;
		public var primaryKey:String;
		public var campaignUID:String;
		public var incomingAddress:String;
		public var incomingAddressType:String;
		public var clientName:String;
	
		public function KeywordVO()
		{
		}

		public function get name():String
		{
			return keyword;
		}
	}
}