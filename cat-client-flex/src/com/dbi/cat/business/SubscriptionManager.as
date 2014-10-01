package com.dbi.cat.business
{
	import com.dbi.cat.common.vo.CampaignVO;
	import com.dbi.cat.common.vo.SubscriberVO;
	import com.dbi.cat.event.CampaignEvent;
	import com.dbi.cat.event.SubscriptionEvent;
	import com.dbi.cat.view.reporting.NodeSubscriberAddressView;
	
	import flash.display.DisplayObject;
	import flash.events.IEventDispatcher;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IViewCursor;
	import mx.core.Application;
	import mx.core.IFlexDisplayObject;
	import mx.managers.PopUpManager;
	
	[Bindable]
	public class SubscriptionManager
	{
		private var dispatcher:IEventDispatcher;
		private var subscriberNodePopup:IFlexDisplayObject;
		
		public var isLoadingNodeSubscriberAddresses:Boolean = false;
		public var nodeSubscribers:ArrayCollection;
		
		
		public function SubscriptionManager(dispatcher:IEventDispatcher)
		{
			this.dispatcher = dispatcher;
		}
		
		public function openNodeSubscribers(nodeUID:String):void
		{
			nodeSubscribers = null;
			isLoadingNodeSubscriberAddresses = true;
			
			// Fire load event
			var event:SubscriptionEvent = new SubscriptionEvent(SubscriptionEvent.LOAD_NODE_SUBSCRIBERS);
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
			nodeSubscribers = null;
			PopUpManager.removePopUp(subscriberNodePopup);
		}
		public function loadNodeSubscribers(subscribers:ArrayCollection):void
		{
			nodeSubscribers = subscribers;
			isLoadingNodeSubscriberAddresses = false;
		}
		public function unsubscribeSubscribers(unsubscribedSubscribers:ArrayCollection, campaign:CampaignVO):void
		{
			for each (var subscriberId:Number in unsubscribedSubscribers)
			{
				var cur:IViewCursor = nodeSubscribers.createCursor();
				while (cur.current != null)
				{
					if (subscriberId == cur.current.subscriberId)
					{
						cur.remove();
						break;
					}
					cur.moveNext();
				}
			}
			isLoadingNodeSubscriberAddresses = false;
			
			// Fire event to reload subscriber statistics
			var statistics:CampaignEvent = new CampaignEvent(CampaignEvent.LOAD_SUBSCRIBER_STATISTICS);
			statistics.campaign = campaign;
			dispatcher.dispatchEvent(statistics);
		}
	}
}