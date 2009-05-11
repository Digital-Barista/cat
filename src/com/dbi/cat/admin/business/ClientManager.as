package com.dbi.cat.admin.business
{
	import com.dbi.cat.admin.view.client.EditClientView;
	import com.dbi.cat.admin.view.client.EditEntryPointView;
	import com.dbi.cat.admin.view.client.EditKeywordView;
	import com.dbi.cat.common.vo.ClientVO;
	import com.dbi.cat.common.vo.EntryPointDefinitionVO;
	import com.dbi.cat.common.vo.KeywordVO;
	
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
		public var clientsGrouped:HierarchicalData;
		public var currentClient:ClientVO;
		public var currentEntryPointDefinition:EntryPointDefinitionVO;
		public var currentKeyword:KeywordVO;
		
		private var editClientPopup:IFlexDisplayObject;
		private var editEntryPointPopup:IFlexDisplayObject;
		private var editKeywordPopup:IFlexDisplayObject;
		
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
			setupGroupedClients();
		}
		private function setupGroupedClients():void
		{
			clientsGrouped = new HierarchicalData(clients);
		}
		
		public function editClient(client:ClientVO):void
		{
			currentClient = client;
			
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
					c.name = client.name;
					found = true;
					break;
				}
			}
			if (!found)
				clients.addItem(client);
			closeEditClient();
		}
		
		//
		// Entry point methods
		//
		public function editEntryPoint(entry:EntryPointDefinitionVO, client:ClientVO):void
		{
			// Assign current objects being edited
			currentEntryPointDefinition = ObjectUtil.copy(entry) as EntryPointDefinitionVO;
			currentClient = client;
			
			
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
			var found:Boolean = false;
			for each (var e:EntryPointDefinitionVO in currentClient.entryPoints)
			{
				if (e.primaryKey == entry.primaryKey)
				{
					e.description = entry.description;
					e.restriction = entry.restriction;
					e.type = entry.type;
					e.value = entry.value;
					found = true;
					break;
				}
			}
			if (!found)
				currentClient.entryPoints.addItem(entry);
				
			setupGroupedClients();
			closeEditEntryPoint();
		}
		
		//
		// Keyword methods
		//
		public function editKeyword(keyword:KeywordVO, entry:EntryPointDefinitionVO):void
		{
			currentKeyword = ObjectUtil.copy(keyword) as KeywordVO;
			currentEntryPointDefinition = entry;
			
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
			var cur:IViewCursor = currentEntryPointDefinition.keywords.createCursor();
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
			var found:Boolean = false;
			for each (var k:KeywordVO in currentEntryPointDefinition.keywords)
			{
				if (k.primaryKey == keyword.primaryKey)
				{
					k.keyword = keyword.keyword;
					found = true;
					break;
				}
			}
			if (!found)
				currentEntryPointDefinition.keywords.addItem(keyword);
			setupGroupedClients();
			closeEditKeyword();
		}
	}
}