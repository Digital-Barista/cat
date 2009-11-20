package com.dbi.cat.business
{
	import com.dbi.cat.common.constants.ContactTagType;
	import com.dbi.cat.common.vo.ContactTagVO;
	import com.dbi.cat.common.vo.ContactVO;
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

		public var contacts:ArrayCollection;
		public var contactMap:Object;
		public var currentContact:ContactVO;
		
		public var contactTags:ArrayCollection;
		public var contactTagMap:Object;
		public var contactTagLists:ArrayCollection;
		
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
		public var filterLabel:String = "Showing 0/0";
		
		public function ContactManager(dispatcher:IEventDispatcher)
		{
			this.dispatcher = dispatcher;
		}
		
		
		//
		// Contact methods
		//
		public function loadContacts(contacts:ArrayCollection):void
		{
			// Preserve filter if it exists
			if (this.contacts != null)
				var filter:Function = this.contacts.filterFunction;
			this.contacts = contacts;
			
			if (filter != null)
			{
				this.contacts.filterFunction = filter;
				this.contacts.refresh();
			}
			
			contactMap = new Object();
			for each (var c:ContactVO in this.contacts)
				contactMap[c.contactId] = c;
				
			updateTagCounts();
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
			filterContacts();
			closeContact();
		}
		public function selectContacts(contacts:ArrayCollection):void
		{
			selectedContacts = contacts;
		}
		public function deleteContact(contact:ContactVO):void
		{
			var cur:IViewCursor = contacts.createCursor();
			while (cur.current != null)
			{
				if (cur.current.contactId == contact.contactId)
				{
					cur.remove();
					break;
				}
				cur.moveNext();
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
			
			// Build map
			contactTagMap = new Object();
			for each (var c:ContactTagVO in this.contactTags)
				contactTagMap[c.contactTagId] = c;
				
			// Build lists for interface
			var userTags:ArrayCollection = new ArrayCollection();
			var systemTags:ArrayCollection = new ArrayCollection();
			for each (var ct:ContactTagVO in contactTags)
			{
				if (ct.type == ContactTagType.USER)
					userTags.addItem(ct);
				else
					systemTags.addItem(ct);
			}
			
			// Sort user tag list which can change
			sort = new Sort();
			sort.fields = [new SortField("tag", true)];
			userTags.sort = sort;
			userTags.refresh();
			
			contactTagLists = new ArrayCollection(
				[{tag:'User Defined Tags', children:userTags},
				{tag:'System Tags', children:systemTags}]);
				
			updateTagCounts();
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
				contactTagLists.getItemAt(0).children.addItem(tag);
				
				// Force rebinding of contactTagLists
				var temp:ArrayCollection = contactTagLists;
				contactTagLists = null;
				contactTagLists = temp;
				
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
			filterContacts();
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
			filterContacts();
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
		public function updateTagCounts():void
		{
			// FIX THIS AT SOME POINT
			return;
			
			if (contacts != null &&
				contactTags != null)
			{
				// Zero counts
				for each (var zero:ContactTagVO in contactTags)
				{
					zero.contactCount = 0;
				}
				
				// Increment counts
				for each (var c:ContactVO in contacts)
				{
					for each (var tag:ContactTagVO in c.contactTags)
					{
						var t:ContactTagVO = contactTagMap[tag.contactTagId];
						t.contactCount++;
					}
				}
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
		public function filterContacts(clientId:String=null, contactType:String=null, contactTags:ArrayCollection=null):void
		{
			// Set filters
			if (clientId != null)
				filterClientId = clientId;
			if (contactType != null)
				filterContactType = contactType;
			if (contactTags != null)
				filterContactTags = contactTags;
			
			// Do filter
			contacts.filterFunction = filterContactsFunction;
			contacts.refresh();
			
			// Update filter label
			filterLabel = "Showing 0/0";
			if (contacts != null)
				filterLabel = "Showing " + contacts.length + "/" + contacts.source.length;
			
			// Close the filter popup		
			closeContactTagFilter();
		}
		private function filterContactsFunction(contact:ContactVO):Boolean
		{
			if (filterClientId != null &&
				filterClientId.length > 0 &&
				filterClientId != contact.clientId)
				return false;
				
			if (filterContactType != null &&
				filterContactType.length > 0 &&
				filterContactType != contact.type)
				return false;
				
			if (filterContactTags != null &&
				filterContactTags.length > 0)
			{
				for each (var filterTag:ContactTagVO in filterContactTags)
				{
					var found:Boolean = false;
					for each (var tag:ContactTagVO in contact.contactTags)
					{
						if (tag.tag.toLowerCase() == filterTag.tag.toLowerCase())
						{
							found = true;
							break;
						}
					}
					if (found)
						break;
				}
				if (!found)
					return false;
			}
			
			return true;
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
			for each (var contact:ContactVO in successfulContactImportList)
				contacts.addItem(contact);

			filterContacts();
			closeContactImport();
			CustomMessage.show("Successfully imported " + successfulContactImportList.length +
				" of " + contactImportList.length + " contacts");
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
