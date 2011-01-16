package com.dbi.cat.business
{
	import com.dbi.cat.common.constants.EntryPointType;
	import com.dbi.cat.common.vo.CampaignVO;
	import com.dbi.cat.common.vo.EntryDataVO;
	import com.dbi.cat.common.vo.NodeVO;
	import com.dbi.cat.common.vo.criteria.ContactSearchCriteriaVO;
	import com.dbi.cat.event.CampaignEvent;
	import com.dbi.cat.event.ContactEvent;
	import com.dbi.cat.view.campaign.ImportSubscribers;
	import com.dbi.controls.CustomMessage;
	
	import flash.display.DisplayObject;
	import flash.events.IEventDispatcher;
	import flash.net.FileReference;
	
	import mx.collections.ArrayCollection;
	import mx.core.Application;
	import mx.core.IFlexDisplayObject;
	import mx.managers.PopUpManager;
	
	[Bindable]
	public class ImportManager
	{
		private var fileRef:FileReference;
		private var importSubscribersPopup:ImportSubscribers;
		private var dispatcher:IEventDispatcher;
		
		public var currentNode:NodeVO;
		public var publishedCampaign:CampaignVO;
		
		public function ImportManager(dispatcher:IEventDispatcher)
		{
			this.dispatcher = dispatcher;
		}

		
		//
		// Import contact subscriber methods
		//
		public function openImportSubscribers(node:NodeVO):void
		{
			currentNode = node;
			
			if (importSubscribersPopup == null)
			{
				importSubscribersPopup = new ImportSubscribers();
			}
			
			// Find entry types for this campaign to restrict by initially
			var types:ArrayCollection = new ArrayCollection();
			for each (var n:* in publishedCampaign.nodes)
			{
				if (n.hasOwnProperty("entryData"))
				{
					for each (var ed:EntryDataVO in n.entryData)
					{
						if (ed.valid)
						{
							if (!types.contains(ed.entryType))
							{
								types.addItem(ed.entryType);
							}
						}
					}
				}
			}
			
			// If there are types change the search filter
			if (types.length > 0)
			{
				var search:ContactSearchCriteriaVO = new ContactSearchCriteriaVO();
				search.entryTypes = types;
				
				var event:ContactEvent = new ContactEvent(ContactEvent.CHANGE_SEARCH_CRITERIA);
				event.searchCriteria = search;
				dispatcher.dispatchEvent(event);
			}
			
			// Add popup
			PopUpManager.removePopUp(importSubscribersPopup);
			PopUpManager.addPopUp(importSubscribersPopup, DisplayObject(Application.application), true);
			PopUpManager.centerPopUp(importSubscribersPopup);
		}
		public function closeImportSubscribers():void
		{
			PopUpManager.removePopUp(importSubscribersPopup);
		}
		public function importSuccessfull():void
		{
			closeImportSubscribers();
			CustomMessage.show("Successfully subscribed contacts");
			
			// Reload the statistics from the current campaign
			var event:CampaignEvent = new CampaignEvent(CampaignEvent.LOAD_SUBSCRIBER_STATISTICS);
			event.campaign = publishedCampaign;
			dispatcher.dispatchEvent(event);
		}
	}
}