package com.dbi.cat.admin.business
{
	import com.dbi.cat.admin.view.client.AssociateEntryPointView;
	import com.dbi.cat.admin.view.client.EditClientView;
	import com.dbi.cat.admin.view.client.EditEntryPointView;
	import com.dbi.cat.admin.view.keyword.EditKeywordView;
	import com.dbi.cat.common.vo.ClientVO;
	import com.dbi.cat.common.vo.EntryPointDefinitionVO;
	import com.dbi.cat.common.vo.KeywordVO;
	import com.dbi.cat.common.vo.ReservedKeywordVO;
	
	import mx.collections.ArrayCollection;
	import mx.collections.HierarchicalData;
	import mx.collections.IViewCursor;
	import mx.core.Application;
	import mx.core.IFlexDisplayObject;
	import mx.core.UIComponent;
	import mx.managers.PopUpManager;
	import mx.utils.ObjectUtil;
	
	[Bindable]
	public class ClientManager
	{
		public var clients:ArrayCollection;
		public var clientMap:Object;
		public var clientsGrouped:HierarchicalData;
		public var entryPointDefinitions:ArrayCollection;
		public var currentClient:ClientVO;
		public var currentEntryPointDefinition:EntryPointDefinitionVO;
		public var currentKeyword:KeywordVO;
		public var keywords:ArrayCollection;
		public var reservedKeywords:ArrayCollection;
		
		private var editClientPopup:IFlexDisplayObject;
		private var editEntryPointPopup:IFlexDisplayObject;
		private var editKeywordPopup:IFlexDisplayObject;
		private var associateEntryPointPopup:IFlexDisplayObject;
		
		public function ClientManager()
		{
		}

		private function clearEditValues():void
		{
			currentKeyword = null;
			currentEntryPointDefinition = null;
			currentClient = null;
		}
		
		//
		// Client methods
		//
		public function loadClients(list:ArrayCollection):void
		{
			clients = list;
			clientMap = new Object();
			for each (var client:ClientVO in clients)
				clientMap[client.clientId] = client;
			setupGroupedClients();
		}
		private function setupGroupedClients():void
		{
			clientsGrouped = new HierarchicalData(clients);
		}
		
		public function editClient(client:ClientVO):void
		{
			currentClient = ObjectUtil.copy(client) as ClientVO;
			
			if (editClientPopup == null)
				editClientPopup = new EditClientView();
			PopUpManager.addPopUp(editClientPopup, UIComponent(Application.application), true);
			PopUpManager.centerPopUp(editClientPopup);
		}
		public function closeEditClient():void
		{
			PopUpManager.removePopUp(editClientPopup);
			clearEditValues();
		}
		public function deleteClient(client:ClientVO):void
		{
			var cur:IViewCursor = clients.createCursor();
			while (cur.current != null)
			{
				if (cur.current.clientId == client.clientId)
					cur.remove();
				else
					cur.moveNext();
			}
		}
		public function saveClient(client:ClientVO):void
		{
			var found:Boolean = false;
			for each (var c:ClientVO in clients)
			{
				if (c.clientId == client.clientId)
				{
					c.active = client.active;
					c.name = client.name;
					c.contactEmail = client.contactEmail;
					c.contactName = client.contactName;
					c.contactPhone = client.contactPhone;
					c.addInMessages = client.addInMessages;
					c.keywordLimits = client.keywordLimits;
					found = true;
					break;
				}
			}
			if (!found)
			{
				clients.addItem(client);
				clientMap[client.clientId] = client;
			}
			closeEditClient();
		}
		
		public function associateEntryPoint(client:ClientVO):void
		{
			currentClient = client;
			
			if (associateEntryPointPopup == null)
				associateEntryPointPopup = new AssociateEntryPointView();
			PopUpManager.addPopUp(associateEntryPointPopup, UIComponent(Application.application), true);
			PopUpManager.centerPopUp(associateEntryPointPopup);
		}
		public function closeAssociateEntryPoint():void
		{
			PopUpManager.removePopUp(associateEntryPointPopup);
		}
		
		//
		// Entry point methods
		//
		public function loadEntryPoints(entryPoints:ArrayCollection):void
		{
			entryPointDefinitions = entryPoints;
		}
		public function editEntryPoint(entry:EntryPointDefinitionVO):void
		{
			// Assign current objects being edited
			currentEntryPointDefinition = ObjectUtil.copy(entry) as EntryPointDefinitionVO;
			
			
			if (editEntryPointPopup == null)
				editEntryPointPopup = new EditEntryPointView();
			PopUpManager.addPopUp(editEntryPointPopup, UIComponent(Application.application), true);
			PopUpManager.centerPopUp(editEntryPointPopup);
		}
		public function closeEditEntryPoint():void
		{
			PopUpManager.removePopUp(editEntryPointPopup);
			clearEditValues();
		}
		public function deleteEntryPoint(entry:EntryPointDefinitionVO):void
		{
			var cur:IViewCursor = currentClient.entryPoints.createCursor();
			while (cur.current != null)
			{
				if (cur.current.primaryKey == entry.primaryKey)
					cur.remove();
				else
					cur.moveNext();
			}
		}
		public function saveEntryPoint(entry:EntryPointDefinitionVO):void
		{
			// Add entry point to list
			var found:Boolean = false;
			for (var i:Number = 0; i < entryPointDefinitions.length; i++)
			{
				if (entryPointDefinitions[i].primaryKey == entry.primaryKey)
				{
					entryPointDefinitions[i] = entry;
					found = true;
					break;
				}
			}
			if (!found)
				entryPointDefinitions.addItem(entry);
			
			// Add entry point to client
			for each (var clientId:Number in entry.clientIDs)
			{
				found = false;
				var client:ClientVO = clientMap[clientId];
				for each (var e:EntryPointDefinitionVO in client.entryPoints)
				{
					if (e.primaryKey == entry.primaryKey)
					{
						e.description = entry.description;
						e.restriction = entry.restriction;
						e.type = entry.type;
						e.value = entry.value;
						e.clientIDs = entry.clientIDs;
						found = true;
						break;
					}
				}
				if (!found)
					client.entryPoints.addItem(entry);
			}
				
			// Unassociate clients
			for each (var c:ClientVO in clients)
			{
				var cur:IViewCursor = c.entryPoints.createCursor();
				while (cur.current != null)
				{
					if (cur.current.primaryKey == entry.primaryKey &&
						!cur.current.clientIDs.contains(c.clientId))
						cur.remove();
					else
						cur.moveNext();
				}
			}
			
			setupGroupedClients();
			closeEditEntryPoint();
			closeAssociateEntryPoint();
		}
		
		//
		// Keyword methods
		//
		public function loadKeywords(keywords:ArrayCollection):void
		{
			this.keywords = keywords;
		}
		public function editKeyword(keyword:KeywordVO, entry:EntryPointDefinitionVO, client:ClientVO):void
		{
			currentKeyword = ObjectUtil.copy(keyword) as KeywordVO;
			currentEntryPointDefinition = entry;
			currentClient = client;
			
			if (editKeywordPopup == null)
				editKeywordPopup = new EditKeywordView();
			PopUpManager.addPopUp(editKeywordPopup, UIComponent(Application.application), true);
			PopUpManager.centerPopUp(editKeywordPopup);
		}
		public function closeEditKeyword():void
		{
			PopUpManager.removePopUp(editKeywordPopup);
			clearEditValues();
		}
		public function deleteKeyword(keyword:KeywordVO):void
		{
			var cur:IViewCursor = keywords.createCursor();
			while (cur.current != null)
			{
				if (cur.current.primaryKey == keyword.primaryKey)
					cur.remove();
				else
					cur.moveNext();
			}
		}
		public function saveKeyword(keyword:KeywordVO):void
		{
			keywords.addItem(keyword);
			setupGroupedClients();
			closeEditKeyword();
		}
	
		//
		// Reserved keyword events
		//
		public function loadReservedKeywords(reservedKeywords:ArrayCollection):void
		{
			this.reservedKeywords = reservedKeywords;
		}
		public function saveReservedKeyword(reservedKeyword:ReservedKeywordVO):void
		{
			var found:Boolean = false;
			for (var i:Number = 0; i < reservedKeywords.length; i++)
			{
				if (reservedKeywords[i].reservedKeywordId == reservedKeyword.reservedKeywordId)
				{
					found = true;
					reservedKeywords[i] = reservedKeyword;
					break;
				}
			}
			if (!found)
				reservedKeywords.addItem(reservedKeyword);
		}
		public function deleteReservedKeyword(reservedKeyword:ReservedKeywordVO):void
		{
			var cur:IViewCursor = reservedKeywords.createCursor();
			while (cur.current != null)
			{
				if (cur.current.reservedKeywordId == reservedKeyword.reservedKeywordId)
					cur.remove();
				else
					cur.moveNext();
			}
		}
	}
}