package com.dbi.cat.common.vo
{
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.Role")]
	public class RoleVO
	{
		public function RoleVO()
		{
		}

		public var primaryKey:Number;
		public var roleName:String;
		public var roleType:String = "user_type";
		public var refId:Number;
	}
}