package com.dbi.cat.business
{
	import com.dbi.cat.common.vo.ContactTagVO;
	import com.dbi.cat.common.vo.ContactVO;
	import com.dbi.cat.view.contacts.EditContactView;
	
	import flash.display.DisplayObject;
	import flash.events.IEventDispatcher;
	
	import mx.collections.ArrayCollection;
	import mx.core.Application;
	import mx.core.IFlexDisplayObject;
	import mx.managers.PopUpManager;
	import mx.utils.ObjectUtil;
	
	[Bindable]
	public class ContactManager
	{
		private var dispatcher:IEventDispatcher;

		public var contacts:ArrayCollection;
		public var contactTags:ArrayCollection;
		public var currentContact:ContactVO;
		
		private var editContactPopup:IFlexDisplayObject;
		
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
		
		public function editContact(contact:ContactVO):void
		{
			currentContact = ObjectUtil.copy(contact) as ContactVO;
			
			if (editContactPopup == null)
				editContactPopup = new EditContactView();
				
			PopUpManager.addPopUp(editContactPopup, DisplayObject(Application.application), true);
			PopUpManager.centerPopUp(editContactPopup);
		}
		public function closeContact():void
		{
			PopUpManager.removePopUp(editContactPopup);
		}
		public function saveContact(contact:ContactVO):void
		{
			if (contact != null)
			{
				contacts.addItem(contact);
			}
			closeContact();
		}
	}
}
