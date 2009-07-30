package com.dbi.cat.event
{
	import com.dbi.cat.common.vo.CampaignVO;
	import com.dbi.cat.common.vo.ConnectorVO;
	import com.dbi.cat.common.vo.NodeVO;
	
	import flash.events.Event;

	public class CampaignEvent extends Event
	{
		public static const LIST_CAMPAIGNS:String = "listCampaignsEvent";
		public static const EDIT_CAMPAIGN:String = "editCampaignEvent";
		public static const CLOSE_EDIT_CAMPAIGN:String = "closeEditCampaignEvent";
		public static const CLOSE_COMMUNICATIONS:String = "closeCommunications";
		public static const LOAD_CAMPAIGN:String = "loadCampaignEvent";
		public static const LOAD_PUBLISHED_CAMPAIGN:String = "loadPublishedCampaignEvent";
		public static const LOAD_MODIFIED_CAMPAIGN:String = "loadModifiedCampaignEvent";
		public static const DELETE_CAMPAIGN:String = "deleteCampaignEvent";
		public static const DELETE_NODE:String = "deleteNodeEvent";
		public static const DELETE_CONNECTOR:String = "deleteConnectorEvent";
		public static const SAVE_CAMPAIGN:String = "saveCampaignEvent";
		public static const SAVE_NODE:String = "saveNodeEvent";
		public static const SAVE_NODE_FAIL:String = "saveNodeFailEvent";
		public static const SAVE_CONNECTOR:String = "saveConnector";
		public static const SAVE_CONNECTOR_FAIL:String = "saveConnectorFail";
		public static const FILTER_CAMPAIGNS:String = "filterCampaignsEvent";
		public static const FILTER_CAMPAIGN_TEMPLATES:String = "filterCampaignTemplatesEvent";
		public static const PUBLISH_CAMPAIGN:String = "publishCampaignEvent";
		public static const LOAD_SUBSCRIBER_STATISTICS:String = "loadSubscriberStatisticsEvent";
	
		public var campaign:CampaignVO;
		public var node:NodeVO;
		public var connector:ConnectorVO;
		public var filterText:String;
			
		public function CampaignEvent(type:String)
		{
			super(type, true, false);
		}
	}
}