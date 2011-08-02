package com.dbi.cat.common.vo
{
	import com.dbi.cat.common.constants.EntryPointType;
	import com.dbi.cat.common.constants.Roles;
	
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
		public var email:String;
		public var password:String;
    public var currentPassword:String;
		public var isActive:Boolean;
		public var roles:ArrayCollection = new ArrayCollection();
		
		public function hasRole(role:String):Boolean
		{
			for each (var r:RoleVO in roles)
			{
				if (r.roleName == role)
					return true;
			}
			return false;
		}
		
		public function get isManager():Boolean
		{
			return hasRole(Roles.ACCOUNT_MANAGER) ||
				hasRole(Roles.ADMIN);
		}
		
		public function get entryPointTypes():ArrayCollection
		{
			var ret:ArrayCollection = EntryPointType.allTypes;
			if (!isManager)
			{
				ret = new ArrayCollection();
				ret.addItem(EntryPointType.FACEBOOK);
				ret.addItem(EntryPointType.TWITTER);
			}
			return ret;
		}
	}
}