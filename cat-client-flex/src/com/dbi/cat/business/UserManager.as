package com.dbi.cat.business
{
	import com.dbi.cat.common.vo.RoleVO;
	import com.dbi.cat.common.vo.UserProfileVO;
	import com.dbi.cat.common.vo.UserVO;
	import com.dbi.cat.event.NavigationEvent;
	import com.dbi.cat.view.user.EditUserView;
	
	import flash.events.IEventDispatcher;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IViewCursor;
	import mx.core.Application;
	import mx.core.IFlexDisplayObject;
	import mx.core.UIComponent;
	import mx.managers.PopUpManager;
	import mx.utils.ObjectUtil;
	
	[Bindable]
	public class UserManager
	{
		private var dispatcher:IEventDispatcher;
		
		public static const RECOVER_PASSWORD_RESET_STATE:String = "resetPassword";

		public var userProfile:UserProfileVO;
		public var userList:ArrayCollection;
		public var userToEdit:UserVO;
		public var availableRoles:ArrayCollection;
		public var filterText:String;
		private var editUserPopup:IFlexDisplayObject;
		
		public function UserManager(dispatcher:IEventDispatcher)
		{
			this.dispatcher = dispatcher;
		}
		
		public function initiateRecoverPassword(emailAddress:String):void
		{
			//Call InitiateRecoverPassword
			var isSuccess:Boolean = true;
			if(isSuccess)
			{
				var navEvent:NavigationEvent = new NavigationEvent(NavigationEvent.RECOVER_PASSWORD_RESET);
				dispatcher.dispatchEvent(navEvent);
			}	
		}
		
		public function resetRecoverPassword(emailAddress:String, recoverPasswordCode:String, 
			password:String, confirmPassword:String):void
		{
			//Call resetRecoverPassword
			var isSuccess:Boolean = true;
			if(isSuccess)
			{
				var navEvent:NavigationEvent = new NavigationEvent(NavigationEvent.CAMPAIGNS);
				dispatcher.dispatchEvent(navEvent);
			}	
		}
		
		public function getUserList(users:ArrayCollection):void
		{
			userList = users;
		}
		
		public function editUser(user:UserVO):void
		{
			userToEdit = ObjectUtil.copy(user) as UserVO;
			
			// Open edit user view as modal popup
			if (editUserPopup == null)
				editUserPopup = new EditUserView();
			else
				closeEditWindow();
				
			PopUpManager.addPopUp(editUserPopup, UIComponent(Application.application), true);
			PopUpManager.centerPopUp(editUserPopup);
		}
		
		public function saveUser(user:UserVO):void
		{
			var found:Boolean = false;
			for (var i:Number = 0; i < userList.length; i++)
			{
				if (userList[i].primaryKey == user.primaryKey)
				{
					userList[i] = user;
					found = true;
					break;
				}
			}
			if (!found)
				userList.addItem(user);
				
			closeEditWindow();
		}
		public function addRole(role:RoleVO):void
		{
			var found:Boolean = false;
			for each (var r:RoleVO in userToEdit.roles)
			{
				if (role.roleName == r.roleName)
				{
					found = true;
					break;
				}
			}
			if (!found)
				userToEdit.roles.addItem(role);
		}
		public function removeRole(role:RoleVO):void
		{
			var cur:IViewCursor = userToEdit.roles.createCursor();
			while (cur.current != null)
			{
				if (cur.current.roleName == role.roleName)
					cur.remove();
				else
					cur.moveNext();
			}
		}
		public function cancelEdit():void
		{
			userToEdit = null;
			closeEditWindow();
		}
		public function deleteUser(userId:Number):void
		{
			var cur:IViewCursor = userList.createCursor();
			while (cur.current != null)
			{
				if (cur.current.primaryKey == userId)
					cur.remove();
				else
					cur.moveNext();
			}
		}
		public function filterUsers(filterText:String):void
		{
			this.filterText = filterText;
			if (userList != null)
			{
				userList.filterFunction = filterUserFunction;
				userList.refresh();
			}
		}
		private function filterUserFunction(user:UserVO):Boolean
		{
			if (filterText == null)
				return true;
				
			if ((user.username != null &&
				user.username.toLowerCase().indexOf(filterText.toLowerCase()) > -1) ||
				
				(user.name != null &&
				user.name.toLowerCase().indexOf(filterText.toLowerCase()) > -1) ||
				
				(user.email != null &&
				user.email.toLowerCase().indexOf(filterText.toLowerCase()) > -1) )
				return true;
				
			return false;
		}
		
		private function closeEditWindow():void
		{
			PopUpManager.removePopUp(editUserPopup);
		}
	}
}
