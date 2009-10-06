package com.dbi.cat.business
{
	import com.dbi.cat.common.vo.CampaignVO;
	import com.dbi.cat.common.vo.ConnectorVO;
	import com.dbi.cat.common.vo.LayoutInfoVO;
	import com.dbi.cat.common.vo.NodeVO;
	import com.dbi.cat.event.LayoutInfoEvent;
	
	import mx.collections.ArrayCollection;
	
	[Bindable]
	public class LayoutInfoManager
	{
		private var dispatcher:IEventDispatcher;
		
		/**
		 * A hash table of LayoutInfo objects indexed
		 * by the UUID of the LayoutInfo
		 */
		public var layoutMap:Object;
		
		/**
		 * References the campaign list from the CampaignManager
		 */
		public var campaignList:ArrayCollection;
		
		/**
		 * References campaign template list from CampaignTemplateManager
		 */
		public var campaignTemplateList:ArrayCollection;
		 
		public function LayoutInfoManager(dispatcher:IEventDispatcher)
		{
			layoutMap = new Object();
			this.dispatcher = dispatcher;
		}

		/**
		 * Replace the current list and map of layouts with a
		 * new one
		 * 
		 * @param layouts ArrayCollection of LayoutInfoVO objects
		 */
		public function loadLayoutInfo(layouts:ArrayCollection):void
		{
			for each (var layout:LayoutInfoVO in layouts)
			{
				layoutMap[layout.key] = layout;
			}
			
			// Try to sync layout info whenever it is loaded
			syncLayoutInfo();
			dispatcher.dispatchEvent(new LayoutInfoEvent(LayoutInfoEvent.LAYOUT_LOADED));
		}
		
		/**
		 * Add a LayoutInfoVO to the stored list and map
		 * 
		 * @parma layout LayoutInfoVO
		 */
		public function saveLayoutInfo(layout:LayoutInfoVO):void
		{
			layoutMap[layout.key] = layout;
		}
		
		/**
		 * Associate all LayoutInfoVOs to each node and connector 
		 * that don't have one.
		 * 
		 */
		public function syncLayoutInfo():void
		{
			if (layoutMap != null)
			{
				if(campaignList != null)
				{
					for each (var campaign:CampaignVO in campaignList)
						setLayouts(campaign);
				}
				
				if (campaignTemplateList != null)
				{
					for each (campaign in campaignTemplateList)
						setLayouts(campaign);
				}
			}
		}
		
		private function setLayouts(campaign:CampaignVO):void
		{
			if (campaign.nodes != null)
			{
				for each (var node:NodeVO in campaign.nodes)
				{
					var key:String = node.uid + ":" + campaign.currentVersion;
					if (node.layoutInfo == null)
						node.layoutInfo = layoutMap[key];
				}
			}
			
			if (campaign.connectors != null)
			{
				for each (var connector:ConnectorVO in campaign.connectors)
				{
					key = connector.uid + ":" + campaign.currentVersion;
					if (connector.layoutInfo == null)
						connector.layoutInfo = layoutMap[key];
				}
			}
		}
	}
}