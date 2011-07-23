package com.dbi.cat.event
{
	import com.dbi.cat.common.vo.RoleVO;
	import com.dbi.cat.common.vo.UserVO;
	
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;

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
    
    public static const UPDATE_CURRENT_USER:String = "updateCurrentUserEvent";
		
		public var user:UserVO;
		public var role:RoleVO;
		public var filterText:String;
		public var clientIds:ArrayCollection;
		
		public function UserEvent(type:String, bubbles:Boolean=true, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
		
		public override function clone():Event
		{
			return new UserEvent(super.type, super.bubbles, super.cancelable);
		}
		
	}
}