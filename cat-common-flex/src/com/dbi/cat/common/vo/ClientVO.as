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
		public var addInMessages:ArrayCollection;
		public var entryPoints:ArrayCollection;
		public var keywordLimits:ArrayCollection;
		
		public function get children():ArrayCollection
		{
			return entryPoints;
		}
	}
}