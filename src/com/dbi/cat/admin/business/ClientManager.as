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

		//
		// Client methods
		//
		public function loadClients(list:ArrayCollection):void
		{
			clients = list;
			
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
			for (var i:Number = 0; i < clients.length; i++)
			{
				if (clients[i].clientId == client.clientId)
				{
					clients[i] = client;
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
		public function editEntryPoint(entry:EntryPointDefinitionVO):void
		{
			// Assign current objects being edited
			currentEntryPointDefinition = ObjectUtil.copy(entry) as EntryPointDefinitionVO;
			for each (var client:ClientVO in clients)
			{
				if (client.entryPoints.contains(entry))
				{
					currentClient = client;
					break;
				}
			}
			
			if (editEntryPointPopup == null)
				editEntryPointPopup = new EditEntryPointView();
			PopUpManager.addPopUp(editEntryPointPopup, UIComponent(Application.application), true);
			PopUpManager.centerPopUp(editEntryPointPopup);
		}
		public function closeEditEntryPoint():void
		{
			PopUpManager.removePopUp(editEntryPointPopup);
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
			for (var i:Number = 0; i < currentClient.entryPoints.length; i++)
			{
				if (currentClient.entryPoints[i].primaryKey == entry.primaryKey)
				{
					currentClient.entryPoints[i] = entry;
					found = true;
					break;
				}
			}
			if (!found)
				currentClient.entryPoints.addItem(entry);
				
			closeEditEntryPoint();
		}
		
		//
		// Keyword methods
		//
		public function editKeyword(keyword:KeywordVO):void
		{
			currentKeyword = ObjectUtil.copy(keyword) as KeywordVO;
			for each (var client:ClientVO in clients)
			{
				var found:Boolean = false;
				for each (var entry:EntryPointDefinitionVO in client.entryPoints)
				{
					if (entry.keywords.contains(entry))
					{
						found = true;
						currentEntryPointDefinition = entry;
						break;
					}
				}
				if (found)
					break;
			}
			
			if (editKeywordPopup == null)
				editKeywordPopup = new EditKeywordView();
			PopUpManager.addPopUp(editKeywordPopup, UIComponent(Application.application), true);
			PopUpManager.centerPopUp(editKeywordPopup);
		}
		public function closeEditKeyword():void
		{
			PopUpManager.removePopUp(editKeywordPopup);
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
			for (var i:Number = 0; i < currentEntryPointDefinition.keywords.length; i++)
			{
				if (currentEntryPointDefinition.keywords[i].primaryKey == keyword.primaryKey)
				{
					currentEntryPointDefinition.keywords[i] = keyword;
					found = true;
					break;
				}
			}
			if (!found)
				currentEntryPointDefinition.keywords.addItem(keyword);
			closeEditKeyword();
		}
	}
}