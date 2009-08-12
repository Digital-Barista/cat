package com.dbi.cat.business
{
	import com.dbi.cat.common.vo.CampaignVO;
	import com.dbi.cat.common.vo.ConnectorVO;
	import com.dbi.cat.common.vo.EntryPointVO;
	import com.dbi.cat.common.vo.NodeVO;
	import com.dbi.cat.event.CampaignEvent;
	import com.dbi.cat.event.ClientEvent;
	import com.dbi.cat.event.LayoutInfoEvent;
	import com.dbi.cat.view.EditCampaignView;
	import com.dbi.cat.view.EditCommunicationsView;
	import com.dbi.controls.CustomMessage;
	
	import flash.display.DisplayObject;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IViewCursor;
	import mx.core.Application;
	import mx.core.IFlexDisplayObject;
	import mx.core.UIComponent;
	import mx.managers.PopUpManager;
	import mx.rpc.Fault;
	import mx.utils.ObjectUtil;
	
	[Bindable]
	public class CampaignManager
	{
		private static const SAVE_ITEM_FAIL:String = "Your changes could not be saved.  " + 
					"You may be experiencing network connectivity issues" + 
					"Please try reloading the application and if the problem persists contact an administrator";
					
		public var modifiedCampaignList:ArrayCollection = new ArrayCollection();
		public var allCampaignList:ArrayCollection = new ArrayCollection();
		public var campaignMap:Object = new Object();
		public var publishedCampaign:CampaignVO;
		public var modifiedCampaign:CampaignVO;
		
		private var editCommunicationPopup:IFlexDisplayObject;
		private var editCampaignPopup:IFlexDisplayObject;
		private var filterText:String;
		private var statistics:Object;
		
		private var dispatcher:IEventDispatcher;
		
		
		public function CampaignManager(dispatcher:IEventDispatcher)
		{
			this.dispatcher = dispatcher;
		}

		//
		// Load methods
		//
		public function loadCampaigns(campaigns:ArrayCollection):void
		{
			modifiedCampaignList = campaigns;
			
			campaignMap = new Object();
			for each (var c:CampaignVO in campaigns)
				campaignMap[c.uid] = c;
		}
		public function loadCampaign(campaign:CampaignVO):void
		{
			if (campaign != null)
			{
				// Update campaign lists
				updateCampaign(campaign);
				
				// Fire event to load layout data
				var layout:LayoutInfoEvent = new LayoutInfoEvent(LayoutInfoEvent.LOAD_CAMPAIGN_LAYOUT_INFO);
				layout.campaign = campaign;
				dispatcher.dispatchEvent(layout);
				
				// Fire event to load subscriber statistics
				var statistics:CampaignEvent = new CampaignEvent(CampaignEvent.LOAD_SUBSCRIBER_STATISTICS);
				statistics.campaign = campaign;
				dispatcher.dispatchEvent(statistics);
			}
			
			// Move to communication screen if not open
			if (editCommunicationPopup == null)
			{
				editCommunicationPopup = new EditCommunicationsView();
				editCommunicationPopup.width = Application.application.width;
				editCommunicationPopup.height = Application.application.height;
			}
			PopUpManager.removePopUp(editCommunicationPopup);
			PopUpManager.addPopUp(editCommunicationPopup, UIComponent(Application.application), true);	
			PopUpManager.centerPopUp(editCommunicationPopup);
			
		}
		public function loadModifiedCampaign(campaign:CampaignVO):void
		{
			modifiedCampaign = campaign;
			loadCampaign(campaign);
		}
		public function loadPublishedCampaign(campaign:CampaignVO):void
		{
			publishedCampaign = campaign;
			loadCampaign(campaign);
		}
		public function loadSubscriberStatistics(stats:Object):void
		{
			statistics = stats;
			syncStatistics();
		}
		public function syncStatistics():void
		{
			for each (var campaign:CampaignVO in allCampaignList)
			{
				for each (var node:NodeVO in campaign.nodes)
				{
					if (statistics.hasOwnProperty(node.uid))
						node.subscriberCount = statistics[node.uid];
				}
			}
			for each (campaign in modifiedCampaignList)
			{
				for each (node in campaign.nodes)
				{
					if (statistics.hasOwnProperty(node.uid))
						node.subscriberCount = statistics[node.uid];
				}
			}
		}
		
		//
		// Filter methods
		//
		public function filterCampaigns(filterText:String):void
		{
			this.filterText = filterText;
			if (modifiedCampaignList != null)
			{
				modifiedCampaignList.filterFunction = filterCampaignsFunction;
				modifiedCampaignList.refresh();
			}
		}
		private function filterCampaignsFunction(campaign:CampaignVO):Boolean
		{
			if (filterText == null ||
				campaign.name.toLowerCase().indexOf(filterText.toLowerCase()) > -1)
				return true;
			return false;
		}
		
		
		//
		// Save success methods
		//
		public function saveCampaign(campaign:CampaignVO):void
		{
			// Update campaign lists
			updateCampaign(campaign);
							
			// Close popup
			closeEditCampaign();
		}
		public function saveNode(node:NodeVO):void
		{
			if (modifiedCampaign != null)
			{
				// Look for an existing node to update
				var found:Boolean = false;
				for (var i:Number = 0;  i < modifiedCampaign.nodes.length; i++)
				{
					var n:NodeVO = modifiedCampaign.nodes[i];
					if (node.uid == n.uid)
					{
						found = true;
						modifiedCampaign.nodes[i] = node;
						break;
					}
				}
				
				// Add a node that is new
				if (!found)
					modifiedCampaign.nodes.addItem(node);
			}
			
		}
		public function saveConnector(connector:ConnectorVO):void
		{
			if (modifiedCampaign != null)
			{
				// Look for an existing connector to update
				var found:Boolean = false;
				for (var i:Number = 0;  i < modifiedCampaign.connectors.length; i++)
				{
					var c:ConnectorVO = modifiedCampaign.connectors[i];
					if (connector.uid == c.uid)
					{
						found = true;
						modifiedCampaign.connectors[i] = connector;
						
						// Make sure connector assoications are up to date
						for each (var node:NodeVO in modifiedCampaign.nodes)
						{
							if (node.uid == connector.destinationUID &&
								!node.upstreamConnections.contains(connector.uid))
								node.upstreamConnections.addItem(connector.uid);
							else if (node.uid == connector.sourceNodeUID &&
								!node.downstreamConnections.contains(connector.uid))
								node.downstreamConnections.addItem(connector.uid);
						}
						break;
					}
				}
				// Add a connector that is new
				if (!found)
					modifiedCampaign.connectors.addItem(connector);
			}
		}
		
		//
		// Save fail methods
		//
		public function campaignModificationFail(fault:Fault):void
		{
			CustomMessage.show(SAVE_ITEM_FAIL);
			
			// Force modified campaign to be reinjected to reload the view
			var temp:CampaignVO = modifiedCampaign;
			modifiedCampaign = null;
			modifiedCampaign = temp;
		}
		
		//
		// Delete success methods
		//
		public function deleteCampaign(campaign:CampaignVO):void
		{
			// Delete from modified campaigns
			var cur:IViewCursor = modifiedCampaignList.createCursor();
			while (cur.current != null)
			{
				if (cur.current.uid == campaign.uid)
					cur.remove();
				else
					cur.moveNext();
			}
			
			// Delete from all campaign list
			cur = allCampaignList.createCursor();
			while (cur.current != null)
			{
				if (cur.current.uid == campaign.uid)
					cur.remove();
				else
					cur.moveNext();
			}
		}
		public function deleteNode(node:NodeVO):void
		{
			if (modifiedCampaign != null)
			{
				// Remove node from campaign
				var cur:IViewCursor = modifiedCampaign.nodes.createCursor();
				while (cur.current != null)
				{
					if (cur.current.uid == node.uid)
						cur.remove();
					else
						cur.moveNext();
				}
				
				// Remove this node from connection lists
				for each (var connector:ConnectorVO in modifiedCampaign.connectors)
				{
					for each (var uid:String in node.downstreamConnections)
					{
						if (connector.uid == uid)
							connector.destinationUID = null;
					}
					for each (uid in node.upstreamConnections)
					{
						if (connector.uid == uid)
							connector.sourceNodeUID = null;
					}
				}
			}
			else
			{
				throw new Error("There is no current campaign to remove the node from");
			}
		}
		public function deleteConnector(connector:ConnectorVO):void
		{
			if (modifiedCampaign != null)
			{
				var cur:IViewCursor = modifiedCampaign.connectors.createCursor();
				while (cur.current != null)
				{
					if (cur.current.uid == connector.uid)
						cur.remove();
					else
						cur.moveNext();
				}
					
				// Remove reference from nodes
				for each (var node:NodeVO in modifiedCampaign.nodes)
				{
					if (node.uid == connector.sourceNodeUID)
					{
						node.downstreamConnections.removeItemAt(
							node.downstreamConnections.getItemIndex(connector.uid));
					}
					if (node.uid == connector.destinationUID)
					{
						node.upstreamConnections.removeItemAt(
							node.upstreamConnections.getItemIndex(connector.uid));
					}
				}
			}
			else
			{
				throw new Error("There is no current campaign to remove the connector from");
			}
		}
		
		//
		// Update methods
		//
		public function editCampaign(campaign:CampaignVO):void
		{
			// Create view
			if (editCampaignPopup == null)
			{
				editCampaignPopup = new EditCampaignView();
			}
			else
			{
				closeEditCampaign();
			}
					
			EditCampaignView(editCampaignPopup).campaign = ObjectUtil.copy(campaign) as CampaignVO;	
				
			// Add popup to application
			PopUpManager.addPopUp(editCampaignPopup, DisplayObject(Application.application), true);
			PopUpManager.centerPopUp(editCampaignPopup);
		}
		public function updateCampaign(campaign:CampaignVO):void
		{
			if (campaign != null)
			{
				// Replace a modified campaign in the stored list with this one
				var found:Boolean = false;
				for (var i:Number = 0; i < modifiedCampaignList.length; i++)
				{
					// Replace the campaign in the list with the newest version
					if (modifiedCampaignList[i].uid == campaign.uid)
					{
						found = true;
						if (modifiedCampaignList[i].currentVersion <= campaign.currentVersion)
						{
							modifiedCampaignList[i] = campaign;
							break;
						}
					}
				}
				// If no version of this campaign UID is found it must be new so add it
				if (!found)
					modifiedCampaignList.addItem(campaign);
				
				// Update campaign map
				campaignMap[campaign.uid] = campaign;
				
				// Make sure this campaign is in the allCampaignsList
				found = false;
				for (i = 0; i < allCampaignList.length; i++)
				{
					if (allCampaignList[i].uid == campaign.uid &&
						allCampaignList[i].currentVersion == campaign.currentVersion)
					{
						found = true;
						allCampaignList[i] = campaign;
						break;
					}
				}
				if (!found)
					allCampaignList.addItem(campaign);
			}
		}
		public function publishCampaign(campaign:CampaignVO):void
		{
			// After publish reload modified and published campaigns
			var event:CampaignEvent = new CampaignEvent(CampaignEvent.LOAD_MODIFIED_CAMPAIGN);
			event.campaign = campaign;
			dispatcher.dispatchEvent(event);
			
			event = new CampaignEvent(CampaignEvent.LOAD_PUBLISHED_CAMPAIGN);
			event.campaign = campaign;
			dispatcher.dispatchEvent(event);
		}
		
		//
		// Close popup methods
		//
		public function closeCommunications():void
		{
			modifiedCampaign = null;
			publishedCampaign = null;
			PopUpManager.removePopUp(editCommunicationPopup);
			
			// If saved node was an entry point query for clients in case
			// keyword assignments have changed
			dispatcher.dispatchEvent(new ClientEvent(ClientEvent.LIST_CLIENTS));
		}
		public function closeEditCampaign():void
		{
			PopUpManager.removePopUp(editCampaignPopup);
		}
		
	}
}