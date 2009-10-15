package com.dbi.cat.event
{
	import com.dbi.cat.common.vo.ClientVO;
	import com.dbi.cat.common.vo.KeywordVO;
	
	import flash.events.Event;

	public class ClientEvent extends Event
	{
		public static const LIST_CLIENTS:String = "listClientsEvent";
		public static const CHANGE_CLIENT:String = "changeClientEvent";
		public static const EDIT_CLIENT:String = "editClientEvent";
		public static const CANCEL_EDIT:String = "cancelEditClientEvent";
		public static const SAVE_CLIENT:String = "saveClientEvent";
		
		public static const SAVE_KEYWORD:String = "saveKeywordEvent";
		public static const EDIT_KEYWORD:String = "editKeywordEvent";
		public static const DELETE_KEYWORD:String = "deleteKeywordEvent";
		public static const CLOSE_EDIT_KEYWORD:String = "closeEditKeywordEvent";
		public static const LIST_KEYWORDS:String = "listKeywordsEvent";
		public static const CHECK_KEYWORD_AVAILABILITY:String = "checkKeywordAvailabilityEvent";
		
		public var client:ClientVO;
		public var keyword:KeywordVO;
		
		public function ClientEvent(type:String)
		{
			super(type, true, false);
		}
		
	}
}