package com.dbi.cat.admin.event
{
	import com.dbi.cat.common.vo.ClientVO;
	import com.dbi.cat.common.vo.EntryPointDefinitionVO;
	import com.dbi.cat.common.vo.FacebookAppVO;
	import com.dbi.cat.common.vo.KeywordVO;
	import com.dbi.cat.common.vo.ReservedKeywordVO;
	
	import flash.events.Event;

	public class AccountEvent extends Event
	{
		public static const LIST_FACEBOOK_ACCOUNTS:String = "listFacebookAccountsEvent";
		public static const EDIT_FACEBOOK_ACCOUNT:String = "editFacebookAccountEvent";
		public static const CLOSE_EDIT_FACEBOOK_ACCOUNT:String = "closeEditFacebookAccountEvent";
		public static const SAVE_FACEBOOK_ACCOUNT:String = "saveFacebookAccountEvent";
		public static const DELETE_FACEBOOK_ACCOUNT:String = "deleteFacebookAccountEvent";
		
		public var facebookApp:FacebookAppVO;
		
		public function AccountEvent(type:String)
		{
			super(type, true, false);
		}
		
	}
}