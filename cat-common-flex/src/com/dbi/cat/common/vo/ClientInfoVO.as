package com.dbi.cat.common.vo
{
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.ClientInfoVO")]
	public class ClientInfoVO
	{
		
		public static const MESSAGE_CREDITS:String = "messageCredits";
		
		public var clientInfoId:String;
		public var clientId:String;
		public var entryType:String;
		public var name:String;
		public var value:String;
	
		public function ClientInfoVO()
		{
		}

	}
}