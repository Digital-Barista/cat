package com.dbi.cat.common.vo
{
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.ClientInfoVO")]
	public class ClientInfoVO
	{
		
		public static const KEY_MESSAGE_CREDITS:String = "messageCredits";
		public static const KEY_CREDIT_PAYMENT_URL:String = "creditPaymentURL";
		
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