package com.dbi.cat.event
{
	import com.dbi.cat.common.vo.ClientVO;
	import com.dbi.cat.common.vo.EntryPointDefinitionVO;
	import com.dbi.cat.common.vo.KeywordVO;
	
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;

	public class ClientEvent extends Event
	{
		// Client events
		public static const LIST_CLIENTS:String = "listClientsEvent";
		public static const CHANGE_CLIENT:String = "changeClientEvent";
		public static const EDIT_CLIENT:String = "editClientEvent";
		public static const CANCEL_EDIT:String = "cancelEditClientEvent";
		public static const SAVE_CLIENT:String = "saveClientEvent";
		public static const CHANGE_FILTER_CLIENTS:String = "changeFilterClientsEvent";
		
		// Keyword events
		public static const SAVE_KEYWORD:String = "saveKeywordEvent";
		public static const EDIT_KEYWORD:String = "editKeywordEvent";
		public static const DELETE_KEYWORD:String = "deleteKeywordEvent";
		public static const CLOSE_EDIT_KEYWORD:String = "closeEditKeywordEvent";
		public static const LIST_KEYWORDS:String = "listKeywordsEvent";
		public static const CHECK_KEYWORD_AVAILABILITY:String = "checkKeywordAvailabilityEvent";
		
		// Entry point events
		public static const LIST_ENTRY_POINT_DEFINITIONS:String = "listEntryPointDefinitionsEvent";
		public static const EDIT_ENTRY_POINT:String = "editEntryPointEvent";
		public static const SAVE_ENTRY_POINT:String = "saveEntryPointEvent";
		public static const DELETE_ENTRY_POINT:String = "deleteEntryPointEvent";
		public static const CLOSE_EDIT_ENTRY_POINT:String = "closeEditEntryPointEvent";
		
		// Credit events
		public static const OPEN_ADD_CREDITS:String = "openAddCreditsEvent";
		public static const CLOSE_ADD_CREDITS:String = "closeAddCreditsEvent";
		public static const OPEN_PAYMENT_URL:String = "openPaymentURLEvent";
		
    // Account activiation
    public static const ACTIVATE_TWITTER_ACCOUNT:String = "activateTwitterAccountEvent";
		
		public var client:ClientVO;
		public var clients:ArrayCollection;
		public var clientIds:ArrayCollection;
		public var keyword:KeywordVO;
		public var entryPointDefinition:EntryPointDefinitionVO;
		
		public var paymentURL:String;
    public var twitterActiviationCallBackUrl:String;
		
		public function ClientEvent(type:String)
		{
			super(type, true, false);
		}
		
	}
}