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
		
		public var client:ClientVO;
		
		public function ClientEvent(type:String)
		{
			super(type, true, false);
		}
		
	}
}