package com.dbi.cat.business
{
	import com.dbi.cat.common.vo.ClientVO;
	import com.dbi.cat.common.vo.EntryPointDefinitionVO;
	import com.dbi.cat.common.vo.KeywordVO;
	import com.dbi.cat.view.EditClientView;
	
	import mx.collections.ArrayCollection;
	import mx.core.Application;
	import mx.core.IFlexDisplayObject;
	import mx.core.UIComponent;
	import mx.managers.PopUpManager;
	
	[Bindable]
	public class ClientManager
	{
		public var clients:ArrayCollection;
		public var clientMap:Object;
		public var currentClient:ClientVO;
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
	}
}