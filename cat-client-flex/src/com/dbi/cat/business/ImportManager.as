package com.dbi.cat.business
{
	import com.dbi.cat.common.vo.CampaignVO;
	import com.dbi.cat.common.vo.NodeVO;
	import com.dbi.cat.event.CampaignEvent;
	import com.dbi.cat.view.campaign.ImportSubscribers;
	import com.dbi.controls.CustomMessage;
	
	import flash.display.DisplayObject;
	import flash.events.IEventDispatcher;
	import flash.net.FileReference;
	
	import mx.core.Application;
	import mx.core.IFlexDisplayObject;
	import mx.managers.PopUpManager;
	
	[Bindable]
	public class ImportManager
	{
		private var fileRef:FileReference;
		private var importSubscribersPopup:IFlexDisplayObject;
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
				importSubscribersPopup = new ImportSubscribers();
				
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