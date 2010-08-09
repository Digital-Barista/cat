package com.dbi.cat.common.vo
{
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.FacebookApp")]
	public class FacebookAppVO
	{
		public var facebookAppId:String;
		public var apiKey:String;
		public var secret:String;
		public var id:String;
		public var clientId:Number;
		public var clientName:String;
		
		public function FacebookAppVO()
		{
		}

	}
}