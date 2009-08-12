package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.Client")]
	public class ClientVO
	{
		public function ClientVO()
		{
			super();
		}
		
		public var clientId:Number;
		public var name:String;
		public var adminAddInMessage:String;
		public var userAddInMessage:String;
		public var entryPoints:ArrayCollection;
		
		public function get children():ArrayCollection
		{
			return entryPoints;
		}
	}
}