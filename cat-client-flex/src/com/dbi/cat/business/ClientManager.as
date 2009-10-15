package com.dbi.cat.business
{
	import com.dbi.cat.common.vo.ClientVO;
	import com.dbi.cat.common.vo.EntryPointDefinitionVO;
	import com.dbi.cat.common.vo.KeywordVO;
	import com.dbi.cat.view.EditClientView;
	import com.dbi.cat.view.profile.EditKeywordView;
	
	import mx.collections.ArrayCollection;
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
		public var keywords:ArrayCollection;
		
		public var currentClient:ClientVO;
		public var currentEntryPointDefinition:EntryPointDefinitionVO;
		public var currentKeyword:KeywordVO;
		
		private var editKeywordPopup:IFlexDisplayObject;
		private var editClientPopup:IFlexDisplayObject;
		
		public function ClientManager()
		{
		}

		public function loadClients(list:ArrayCollection):void
		{
			clients = list;
			
			// Build client map indexed by clientId
			clientMap = new Object();
			for each (var client:ClientVO in clients)
				clientMap[client.clientId] = client;
			
			// Set the first client as the current when first loaded
			if (clients.length > 0)
				currentClient = clients[0];
		}
		
		public function changeClient(client:ClientVO):void
		{
			currentClient = client;
		}
		
		public function editClient(client:ClientVO):void
		{
			
			if (editClientPopup == null)
				editClientPopup = new EditClientView();
			else
				closeEdit();
				
			EditClientView(editClientPopup).client = client;
			PopUpManager.addPopUp(editClientPopup, UIComponent(Application.application), true);
			PopUpManager.centerPopUp(editClientPopup);
		}
		public function closeEdit():void
		{
			PopUpManager.removePopUp(editClientPopup);
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
				
			clientMap[client.clientId] = client;
			closeEdit();
		}
	
		//
		// Keyword methods
		//
		public function loadKeywords(list:ArrayCollection):void
		{
			keywords = list;
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
			closeEditKeyword();
		}
		private function clearEditValues():void
		{
			currentKeyword = null;
			currentEntryPointDefinition = null;
			currentClient = null;
		}
	}
}