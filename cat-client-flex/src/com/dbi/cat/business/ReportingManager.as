package com.dbi.cat.business
{
	import com.dbi.cat.common.vo.DashboardDataVO;
	import com.dbi.cat.event.ReportingEvent;
	import com.dbi.cat.view.reporting.NodeSubscriberAddressView;
	
	import flash.display.DisplayObject;
	import flash.events.IEventDispatcher;
	
	import mx.collections.ArrayCollection;
	import mx.core.Application;
	import mx.core.IFlexDisplayObject;
	import mx.managers.PopUpManager;
	
	[Bindable]
	public class ReportingManager
	{
		private var dispatcher:IEventDispatcher;
		private var subscriberNodePopup:IFlexDisplayObject;
	
		
		public var isLoadingNodeSubscriberAddresses:Boolean = false;
		public var nodeSubscriberAddresses:ArrayCollection;
		
		public var dashboardData:DashboardDataVO;
		public var outgoingMessageSummaries:ArrayCollection;
		
		public function ReportingManager(dispatcher:IEventDispatcher)
		{
			this.dispatcher = dispatcher;
		}

		//
		// Subscriber node methods
		//
		public function openNodeSubscribers(nodeUID:String):void
		{
			nodeSubscriberAddresses = null;
			isLoadingNodeSubscriberAddresses = true;
			
			// Fire load event
			var event:ReportingEvent = new ReportingEvent(ReportingEvent.LOAD_NODE_SUBSCRIBERS);
			event.nodeUID = nodeUID;
			dispatcher.dispatchEvent(event);
			
			// Open popup
			if (subscriberNodePopup == null)
				subscriberNodePopup = new NodeSubscriberAddressView();
				
			PopUpManager.addPopUp(subscriberNodePopup, DisplayObject(Application.application), true);
			PopUpManager.centerPopUp(subscriberNodePopup);
		}
		public function closeNodeSubscribers():void
		{
			nodeSubscriberAddresses = null;
			PopUpManager.removePopUp(subscriberNodePopup);
		}
		public function loadNodeSubscribers(addresses:ArrayCollection):void
		{
			nodeSubscriberAddresses = addresses;
			isLoadingNodeSubscriberAddresses = false;
		}
		
		//
		// Message reporting methods
		//
		public function loadOutgoingMessageSummaries(summaries:ArrayCollection):void
		{
			outgoingMessageSummaries = summaries;
		}
		public function loadDashboardData(dashboardData:DashboardDataVO):void
		{
			this.dashboardData = dashboardData;
		}
	}
}