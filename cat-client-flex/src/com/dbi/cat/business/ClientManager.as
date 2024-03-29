package com.dbi.cat.business
{
	import com.dbi.cat.common.vo.ClientVO;
	import com.dbi.cat.common.vo.EntryPointDefinitionVO;
	import com.dbi.cat.common.vo.KeywordVO;
	import com.dbi.cat.constants.KeywordAvailableCheckState;
	import com.dbi.cat.view.EditClientView;
	import com.dbi.cat.view.profile.AddCreditsView;
	import com.dbi.cat.view.profile.EditEntryPointView;
	import com.dbi.cat.view.profile.EditKeywordView;
	
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	
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
		public var allClients:ArrayCollection; // Includes an "All Clients" option
		public var filterClients:ArrayCollection; // Globally selected clients to filter by. null == "all"
		public var clientMap:Object;
		public var keywords:ArrayCollection;
		public var entryPointDefinitions:ArrayCollection;
		
		public var currentClient:ClientVO;
		public var currentKeyword:KeywordVO;
		public var currentEntryPointDefinition:EntryPointDefinitionVO;
		public var isKeywordAvailable:String; KeywordAvailableCheckState.STOPPED;
		
		private var editKeywordPopup:IFlexDisplayObject;
		private var editClientPopup:IFlexDisplayObject;
		private var editEntryPointPopup:IFlexDisplayObject;
		private var addCreditsPopup:IFlexDisplayObject;
		
		public function ClientManager()
		{
		}

		public function loadClients(list:ArrayCollection):void
		{
			clients = list;
			allClients = new ArrayCollection([{name: "(All Clients)"}]);
			
			// Build client map indexed by clientId
			clientMap = new Object();
			for each (var client:ClientVO in clients)
			{
				clientMap[client.clientId] = client;
				allClients.addItem(client);
			}
			
			// Set the first client as the current when first loaded
			if (clients.length > 0)
				currentClient = clients[0];
		}
		
		public function setFilterClients(clients:ArrayCollection):void
		{
			filterClients = null;
			filterClients = clients;
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
			
			// If the current client was updated replace it
			if (currentClient.clientId == client.clientId)
			{
				currentClient = null;
				currentClient = client;
			}
			closeEdit();
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
			
			closeEditEntryPoint();
		}
		public function startKeywordAvailabilityCheck():void
		{
			isKeywordAvailable = KeywordAvailableCheckState.CHECKING;
		}
		public function showKeywordAvailability(isAvailable:Boolean):void
		{
			if (isAvailable)
				isKeywordAvailable = KeywordAvailableCheckState.AVAILABLE;
			else
				isKeywordAvailable = KeywordAvailableCheckState.UNAVAILABLE;
		}
		//
		// Keyword methods
		//
		public function loadKeywords(list:ArrayCollection):void
		{
			keywords = list;
		}
		public function editKeyword(keyword:KeywordVO):void
		{
			currentKeyword = ObjectUtil.copy(keyword) as KeywordVO;
			
			if (editKeywordPopup == null)
				editKeywordPopup = new EditKeywordView();
			PopUpManager.addPopUp(editKeywordPopup, UIComponent(Application.application), true);
			PopUpManager.centerPopUp(editKeywordPopup);
			
			isKeywordAvailable = KeywordAvailableCheckState.STOPPED;
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
			currentClient = null;
		}
	
		//
		// Credits methods
		//
		public function openAddCredits():void
		{
			if (addCreditsPopup == null)
				addCreditsPopup = new AddCreditsView();
			PopUpManager.addPopUp(addCreditsPopup, UIComponent(Application.application), true);
			PopUpManager.centerPopUp(addCreditsPopup);
		}
		public function closeAddCredits():void
		{
			PopUpManager.removePopUp(addCreditsPopup);
		}
		public function openPaymentURL(url:String):void
		{
			navigateToURL(new URLRequest(url));
		}
	
  
    //
    // Account activation
    //
    public function activateTwitterAccount(activateUrl:String):void
    {
        navigateToURL(new URLRequest(activateUrl));
    }
  }
}