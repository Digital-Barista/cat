package com.dbi.cat.business
{
	import com.dbi.cat.common.constants.ContactTagType;
	import com.dbi.cat.common.vo.ContactTagVO;
	import com.dbi.cat.common.vo.ContactVO;
	import com.dbi.cat.common.vo.PagedListVO;
	import com.dbi.cat.common.vo.PagingInfoVO;
	import com.dbi.cat.common.vo.criteria.ContactSearchCriteriaVO;
	import com.dbi.cat.event.ContactEvent;
	import com.dbi.cat.view.contacts.AddTagView;
	import com.dbi.cat.view.contacts.ContactImportView;
	import com.dbi.cat.view.contacts.EditContactView;
	import com.dbi.cat.view.contacts.FilterTagView;
	import com.dbi.cat.view.contacts.RemoveTagView;
	import com.dbi.controls.CustomMessage;
	
	import flash.display.DisplayObject;
	import flash.events.IEventDispatcher;
	import flash.net.FileReference;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IViewCursor;
	import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.core.Application;
	import mx.core.IFlexDisplayObject;
	import mx.managers.PopUpManager;
	import mx.utils.ObjectUtil;
	import mx.utils.StringUtil;
	
	[Bindable]
	public class ContactManager
	{
		private var dispatcher:IEventDispatcher;

		public var contacts:PagedListVO;
		public var contactMap:Object;
		public var currentContact:ContactVO;
		
		public var contactTags:ArrayCollection;
		public var contactTagMap:Object;
		public var userContactTags:ArrayCollection;
		
		public var selectedContacts:ArrayCollection;
		
		private var editContactPopup:IFlexDisplayObject;
		private var editContactTagAssignmentPopup:IFlexDisplayObject;
		private var editContactTagUnassignmentPopup:IFlexDisplayObject;
		private var contactTagFilterPopup:IFlexDisplayObject;
		private var contactImportPopup:IFlexDisplayObject;
		
		// Import file reference
		private var fileRef:FileReference;
		public var importedContacts:ArrayCollection;
		
		// Filter properties
		private var filterClientId:String;
		private var filterContactType:String;
		public var filterContactTags:ArrayCollection;
		
		// Paging information
		public var contactPagingInfo:PagingInfoVO;
		public var contactSearchCriteria:ContactSearchCriteriaVO;
		public var isSearchingContacts:Boolean = false;
		
		public function ContactManager(dispatcher:IEventDispatcher)
		{
			this.dispatcher = dispatcher;
		}
		
		
		//
		// Contact methods
		//
		public function loadContacts(contacts:PagedListVO, searchCriteria:ContactSearchCriteriaVO, pagingInfo:PagingInfoVO):void
		{
			this.contacts = contacts;
			
			if (pagingInfo != null )
				contactPagingInfo = pagingInfo;
			
			if (searchCriteria != null)
				contactSearchCriteria = searchCriteria;
			
			contactMap = new Object();
			for each (var c:ContactVO in this.contacts.results)
				contactMap[c.contactId] = c;
				
			isSearchingContacts = false;
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
				contacts.results.addItem(contact);
				contactMap[contact.contactId] = contact;
			}
			closeContact();
		}
		public function selectContacts(contacts:ArrayCollection):void
		{
			selectedContacts = contacts;
		}
		public function deleteContact(contact:ContactVO):void
		{
			var cur:IViewCursor = contacts.results.createCursor();
			while (cur.current != null)
			{
				if (cur.current.contactId == contact.contactId)
				{
					cur.remove();
					break;
				}
				cur.moveNext();
			}
			contactMap[contact.contactId] = null;
		}
		public function blacklistAddresses(blacklistContacts:ArrayCollection):void
		{
			for each (var contact:ContactVO in blacklistContacts)
			{
				for each (var existing:ContactVO in contacts.results)
				{
					if (contact.address == existing.address &&
						contact.type == existing.type)
					{
						existing.blacklisted = true;
					}
				}
			}
			CustomMessage.show("Successfully blacklisted selected addresses");
		}
		public function unBlacklistAddresses(blacklistContacts:ArrayCollection):void
		{
			for each (var contact:ContactVO in blacklistContacts)
			{
				for each (var existing:ContactVO in contacts.results)
				{
					if (contact.address == existing.address &&
						contact.type == existing.type)
					{
						existing.blacklisted = false;
					}
				}
			}
		}
		
		//
		// Contact tag methods
		//
		public function loadContactTags(contactTags:ArrayCollection):void
		{
			this.contactTags = contactTags;
			
			// Sort tags
			var sort:Sort = new Sort();
			sort.fields = [new SortField("tag", true)];
			contactTags.sort = sort;
			contactTags.refresh();
			
			// Build map and user tag list
			contactTagMap = new Object();
			userContactTags = new ArrayCollection();
			for each (var c:ContactTagVO in this.contactTags)
			{
				contactTagMap[c.contactTagId] = c;
				if (c.type == ContactTagType.USER)
					userContactTags.addItem(c);
			}
			
			// Sort user tags
			sort = new Sort();
			sort.fields = [new SortField("tag", true)];
			userContactTags.sort = sort;
			userContactTags.refresh();
		}
		public function openContactTagAssignment():void
		{
			if (editContactTagAssignmentPopup == null)
				editContactTagAssignmentPopup = new AddTagView();
				
			PopUpManager.addPopUp(editContactTagAssignmentPopup, DisplayObject(Application.application), true);
			PopUpManager.centerPopUp(editContactTagAssignmentPopup);
		}
		public function closeContactTagAssignment():void
		{
			PopUpManager.removePopUp(editContactTagAssignmentPopup);
		}
		public function openContactTagUnassignment():void
		{
			if (editContactTagUnassignmentPopup == null)
				editContactTagUnassignmentPopup = new RemoveTagView();
				
			PopUpManager.addPopUp(editContactTagUnassignmentPopup, DisplayObject(Application.application), true);
			PopUpManager.centerPopUp(editContactTagUnassignmentPopup);
		}
		public function closeContactTagUnassignment():void
		{
			PopUpManager.removePopUp(editContactTagUnassignmentPopup);
		}
		public function saveTag(tag:ContactTagVO):void
		{
			if (tag != null)
			{
				contactTags.addItem(tag);
				contactTagMap[tag.contactTagId] = tag;
				
				if (tag.type == ContactTagType.USER)
					userContactTags.addItem(tag);
				
			}
		}
		public function addTagsToContacts(contacts:ArrayCollection, tags:ArrayCollection):void
		{
			for each (var c:ContactVO in contacts)
			{
				var existing:ContactVO = contactMap[c.contactId];
				for each (var tag:ContactTagVO in tags)
				{
					var found:Boolean = false;
					for each (var t:ContactTagVO in existing.contactTags)
					{
						if (t.contactTagId == tag.contactTagId)
						{
							found = true;
							break;
						}
					}
					if (!found)
					{
						// Add new tag
						existing.contactTags.addItem(tag);
						existing.tagListLabel = null;
					}
				}
			}
//			filterContacts();
			closeContactTagAssignment();
		}
		public function removeTagsFromContacts(contacts:ArrayCollection, tags:ArrayCollection):void
		{
			for each (var c:ContactVO in contacts)
			{
				var existing:ContactVO = contactMap[c.contactId];
				for each (var tag:ContactTagVO in tags)
				{
					var cur:IViewCursor = existing.contactTags.createCursor();
					while (cur.current != null)
					{
						if (cur.current.contactTagId == tag.contactTagId)
						{
							cur.remove();
							existing.tagListLabel = null;
						}
						else
						{
							cur.moveNext();
						}
					}
				}
			}
//			filterContacts();
			closeContactTagUnassignment();
		}
		public function deleteTag(tag:ContactTagVO):void
		{
			var cur:IViewCursor = contactTags.createCursor();
			while (cur.current != null)
			{
				if (cur.current.contactTagId == tag.contactTagId)
				{
					cur.remove();
					break;
				}
				cur.moveNext();
			}
		}
		public function openContactTagFilter():void
		{
			if (contactTagFilterPopup == null)
				contactTagFilterPopup = new FilterTagView();
				
			PopUpManager.addPopUp(contactTagFilterPopup, DisplayObject(Application.application), true);
			PopUpManager.centerPopUp(contactTagFilterPopup);
		}
		public function closeContactTagFilter():void
		{
			PopUpManager.removePopUp(contactTagFilterPopup);
		}
	
		//
		// Contact import methods
		//
		public function openContactImport():void
		{
			if (contactImportPopup == null)
				contactImportPopup = new ContactImportView();
				
			PopUpManager.addPopUp(contactImportPopup, DisplayObject(Application.application), true);
			PopUpManager.centerPopUp(contactImportPopup);
		}
		public function closeContactImport():void
		{
			PopUpManager.removePopUp(contactImportPopup);
		}
		public function importContacts(contactImportList:ArrayCollection, successfulContactImportList:ArrayCollection):void
		{

			closeContactImport();
			CustomMessage.show("Successfully imported " + successfulContactImportList.length +
				" of " + contactImportList.length + " contacts");
				
			// Refresh list
			var event:ContactEvent = new ContactEvent(ContactEvent.LIST_CONTACTS);
			event.searchCriteria = contactSearchCriteria;
			event.pagingInfo = contactPagingInfo;
			dispatcher.dispatchEvent(event);
		}
		public function selectContactImportFile():void
		{
			fileRef = new FileReference();
			fileRef.addEventListener(Event.SELECT, selectFile);
			fileRef.browse();
		}
		private function selectFile(e:Event):void
		{
			fileRef.load();
			fileRef.addEventListener(Event.COMPLETE, fileUploaded);
		}
		private function fileUploaded(e:Event):void
		{
			importedContacts = new ArrayCollection();
			var lines:Array = fileRef.data.toString().split("\n");
			for each (var s:String in lines)
			{
				importedContacts.addItem({address:StringUtil.trim(s)});
			}
			
			if (importedContacts.length > 0)
			{
				openContactImport();
			}
			else
			{
				CustomMessage.show("No contacts were imported");
			}
		}
	}
}
