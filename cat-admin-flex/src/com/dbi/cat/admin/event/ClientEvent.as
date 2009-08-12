package com.dbi.cat.admin.event
{
	import com.dbi.cat.common.vo.ClientVO;
	import com.dbi.cat.common.vo.EntryPointDefinitionVO;
	import com.dbi.cat.common.vo.KeywordVO;
	
	import flash.events.Event;

	public class ClientEvent extends Event
	{
		public static const LIST_CLIENTS:String = "listClientsEvent";
		public static const EDIT_CLIENT:String = "editClientEvent";
		public static const LOAD_CLIENT:String = "loadClientEvent";
		public static const CLOSE_EDIT_CLIENT:String = "closeEditClientEvent";
		public static const SAVE_CLIENT:String = "saveClientEvent";
		public static const DELETE_CLIENT:String = "deleteClientEvent";
		
		public static const ASSOCIATE_ENTRY_POINT:String = "associateEntryPointEvent";
		public static const UNASSOCIATE_ENTRY_POINT:String = "unassociateEntryPointEvent";
		public static const CLOSE_ASSOCIATE_ENTRY_POINT:String = "closeAssociateEntryPointEvent";
		
		public static const LIST_ENTRY_POINT_DEFINITIONS:String = "listEntryPointDefinitionsEvent";
		public static const EDIT_ENTRY_POINT:String = "editEntryPointEvent";
		public static const SAVE_ENTRY_POINT:String = "saveEntryPointEvent";
		public static const DELETE_ENTRY_POINT:String = "deleteEntryPointEvent";
		public static const CLOSE_EDIT_ENTRY_POINT:String = "closeEditEntryPointEvent";
		
		public static const SAVE_KEYWORD:String = "saveKeywordEvent";
		public static const EDIT_KEYWORD:String = "editKeywordEvent";
		public static const DELETE_KEYWORD:String = "deleteKeywordEvent";
		public static const CLOSE_EDIT_KEYWORD:String = "closeEditKeywordEvent";
		public static const LIST_KEYWORDS:String = "listKeywordsEvent";
		
		public var client:ClientVO;
		public var entryPoint:EntryPointDefinitionVO;
		public var keyword:KeywordVO;
		
		public function ClientEvent(type:String)
		{
			super(type, true, false);
		}
		
	}
}