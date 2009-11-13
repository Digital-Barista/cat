package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.Contact")]
	public class ContactVO
	{
		public var contactId:String;
		public var clientId:String;
		public var type:String; // Contact Type = EntryPointType
		public var address:String;
		public var createDate:Date;
		public var contactTags:ArrayCollection;
		
		public function ContactVO()
		{
		}

	}
}