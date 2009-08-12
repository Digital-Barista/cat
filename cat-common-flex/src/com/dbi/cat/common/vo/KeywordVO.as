package com.dbi.cat.common.vo
{
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.Keyword")]
	public class KeywordVO
	{
		
		public var entryPointId:Number;
		public var clientId:Number;
		public var keyword:String;
		public var primaryKey:Number;
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