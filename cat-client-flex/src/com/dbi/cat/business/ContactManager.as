package com.dbi.cat.business
{
	import com.dbi.cat.common.vo.ContactTagVO;
	
	import flash.events.IEventDispatcher;
	
	import mx.collections.ArrayCollection;
	
	[Bindable]
	public class ContactManager
	{
		private var dispatcher:IEventDispatcher;

		public var contacts:ArrayCollection;
		public var contactTags:ArrayCollection;
		
		public function ContactManager(dispatcher:IEventDispatcher)
		{
			this.dispatcher = dispatcher;
		}
		
		public function loadContacts(contacts:ArrayCollection):void
		{
			this.contacts = contacts;
		}
		public function loadContactTags(contactTags:ArrayCollection):void
		{
			this.contactTags = contactTags;
		}
		
		public function saveTag(tag:ContactTagVO):void
		{
			if (tag != null)
			{
				contactTags.addItem(tag);
			}
		}
	}
}
