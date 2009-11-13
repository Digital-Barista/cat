package com.dbi.cat.event
{
	import com.dbi.cat.common.vo.ContactTagVO;
	
	import flash.events.Event;

	public class ContactEvent extends Event
	{
		public static const LIST_CONTACTS:String = "listContactsEvent";
		public static const LIST_CONTACT_TAGS:String = "listContactTagsEvent";
		public static const SAVE_CONTACT_TAG:String = "saveContactTagEvent";
		
		public var contactTag:ContactTagVO;
		
		public function ContactEvent(type:String)
		{
			super(type, true, false);
		}
		
	}
}