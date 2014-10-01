package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.AddInMessage")]
	public class AddInMessageVO
	{
		public function AddInMessageVO()
		{
			super();
		}
		
		public var addInMessageId:String;
		public var clientId:String;
		public var campaignId:String;
		public var entryType:String;
		public var type:String;
		public var message:String;
	}
}