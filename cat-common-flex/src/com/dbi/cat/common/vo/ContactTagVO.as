package com.dbi.cat.common.vo
{
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.ContactTag")]
	public class ContactTagVO
	{
		public var contactTagId:String;
		public var tag:String;
		public var clientId:String;
		public var type:String;
		public var contactCount:Number = 0;
		public var tagDate:Date;
		
		public function ContactTagVO()
		{
		}

	}
}