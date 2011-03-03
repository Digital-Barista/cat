package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.ContactInfo")]
	public class ContactInfoVO 
	{
		
		public var contactInfoId:Number;
		public var name:String;
		public var value:String;
	
		public function ContactInfoVO()
		{
			super();
		}
	}
}