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
		public var contactName:String;
		public var contactEmail:String;
		public var contactPhone:String;
		public var active:Boolean = true;
	
		public var addInMessages:ArrayCollection;
		public var entryPoints:ArrayCollection;
		public var keywordLimits:ArrayCollection;
		public var clientInfos:ArrayCollection;
		
		public function get children():ArrayCollection
		{
			return entryPoints;
		}
	}
}