package com.dbi.cat.event
{
	import com.dbi.cat.common.vo.ContactTagVO;
	import com.dbi.cat.common.vo.ContactVO;
	
	import flash.events.Event;

	public class ContactEvent extends Event
	{
		public static const LIST_CONTACTS:String = "listContactsEvent";
		public static const LIST_CONTACT_TAGS:String = "listContactTagsEvent";
		public static const SAVE_CONTACT_TAG:String = "saveContactTagEvent";
		
		public static const EDIT_CONTACT:String = "editContactEvent";
		public static const CLOSE_CONTACT:String = "closeContactEvent";
		public static const SAVE_CONTACT:String = "saveContactEvent";
		
		public var contactTag:ContactTagVO;
		public var contact:ContactVO;
		
		public function ContactEvent(type:String)
		{
			super(type, true, false);
		}
		
	}
}