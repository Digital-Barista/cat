package com.dbi.cat.business
{
	import com.dbi.cat.common.constants.ContactTagType;
	import com.dbi.cat.common.vo.ContactTagVO;
	import com.dbi.cat.common.vo.ContactVO;
	import com.dbi.cat.view.contacts.AddTagView;
	import com.dbi.cat.view.contacts.EditContactView;
	
	import flash.display.DisplayObject;
	import flash.events.IEventDispatcher;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IViewCursor;
	import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.core.Application;
	import mx.core.IFlexDisplayObject;
	import mx.managers.PopUpManager;
	import mx.utils.ObjectUtil;
	
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
		
		public function ContactManager(dispatcher:IEventDispatcher)
		{
			this.dispatcher = dispatcher;
		}
		
		
		//
		// Contact methods
		//
		public function loadContacts(contacts:ArrayCollection):void
		{
			this.contacts = contacts;
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
				[{tag:'User Defined', children:userTags},
				{tag:'System', children:systemTags}]);
				
			updateTagCounts();
		}
		public function editContactTagAssignment():void
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
		public function saveTag(tag:ContactTagVO):void
		{
			if (tag != null)
			{
				contactTags.addItem(tag);
				contactTagMap[tag.contactTagId] = tag;
				contactTagLists.getItemAt(0).children.addItem(tag);
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
						
						// Increment tag count
					}
				}
			}
			closeContactTagAssignment();
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
	}
}
