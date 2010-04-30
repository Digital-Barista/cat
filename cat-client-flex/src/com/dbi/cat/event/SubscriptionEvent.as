package com.dbi.cat.event
{
	import com.dbi.cat.common.vo.CampaignVO;
	
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;

	public class SubscriptionEvent extends Event
	{
		public static const UNSUBSCRIBE_SUBSCRIBERS:String = "unsubscribeSubscribersEvent";
		public static const LOAD_NODE_SUBSCRIBERS:String = "loadNodeSubscribersEvent";
		public static const OPEN_NODE_SUBSCRIBERS:String = "openNodeSubscribersEvent";
		public static const CLOSE_NODE_SUBSCRIBERS:String = "closeNodeSubscribersEvent";
		
		public var nodeUID:String;
		public var subscriberIds:ArrayCollection;
		public var campaign:CampaignVO;
		
		public function SubscriptionEvent(type:String)
		{
			super(type, true, false);
		}
		
	}
}