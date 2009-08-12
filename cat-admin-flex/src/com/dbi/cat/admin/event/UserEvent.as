package com.dbi.cat.admin.event
{
	import com.dbi.cat.common.vo.RoleVO;
	import com.dbi.cat.common.vo.UserVO;
	
	import flash.events.Event;

	public class UserEvent extends Event
	{
		public static const ADD_ROLE:String = "addRoleEvent";
		public static const REMOVE_ROLE:String = "removeRoleEvent";
		public static const SAVE_USER:String = "saveUserEvent";
		public static const DELETE_USER:String = "deleteUserEvent";
		public static const RESET_EDIT_PROFILE:String = "resetEditProfileEvent";
		public static const GET_USER_LIST:String = "getUserListEvent";
		public static const EDIT_USER:String = "editUserEvent"
		public static const GET_CURRENT_USER:String = "getCurrentUser"
		public static const CANCEL_EDIT:String = "cancelEditUserEvent"
		public static const FILTER_USERS:String = "filterUsersEvent"
		
		public var user:UserVO;
		public var role:RoleVO;
		public var filterText:String;
		
		public function UserEvent(type:String)
		{
			super(type, true, false);
		}
		
		public override function clone():Event
		{
			return new UserEvent(super.type);
		}
		
	}
}