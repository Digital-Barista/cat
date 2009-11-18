package com.dbi.cat.event
{
	import com.dbi.cat.common.vo.ContactTagVO;
	import com.dbi.cat.common.vo.ContactVO;
	
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;

	public class ContactEvent extends Event
	{
		public static const LIST_CONTACTS:String = "listContactsEvent";
		public static const LIST_CONTACT_TAGS:String = "listContactTagsEvent";
		public static const SAVE_CONTACT_TAG:String = "saveContactTagEvent";
		public static const DELETE_CONTACT_TAG:String = "deleteContactTagEvent";
		public static const ASSIGN_CONTACT_TAGS:String = "assignContactTagsEvent";
		public static const UNASSIGN_CONTACT_TAGS:String = "unassignContactTagsEvent";
		public static const CLOSE_CONTACT_TAG_ASSIGNMENT:String = "closeContactTagAssignmentEvent";
		
		public static const OPEN_CONTACT_TAG_FILTER:String = "openContactTagFiltertEvent";
		public static const CLOSE_CONTACT_TAG_FILTER:String = "closeContactTagFiltertEvent";
		public static const FILTER_CONTACTS:String = "filterContactsEvent";
		
		
		public static const EDIT_CONTACT:String = "editContactEvent";
		public static const CLOSE_CONTACT:String = "closeContactEvent";
		public static const SAVE_CONTACT:String = "saveContactEvent";
		public static const SELECT_CONTACTS:String = "selectContactEvent";
		public static const ADD_TAGS_TO_CONTACTS:String = "addTagsToContactEvent";
		public static const DELETE_CONTACT:String = "deleteContactEvent";
		
		public var contactTag:ContactTagVO;
		public var contact:ContactVO;
		
		public var contactTags:ArrayCollection;
		public var contacts:ArrayCollection;
		
		public var clientId:String;
		public var contactType:String;
		
		public function ContactEvent(type:String)
		{
			super(type, true, false);
		}
		
	}
}