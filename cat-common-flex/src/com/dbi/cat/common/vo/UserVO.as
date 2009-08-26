package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.User")]
	public class UserVO
	{
		public function UserVO()
		{
		}

		public var primaryKey:Number;
		public var username:String;
		public var name:String;
		public var emailAddress:String;
		public var password:String;
		public var isActive:Boolean;
		public var roles:ArrayCollection = new ArrayCollection();
	}
}