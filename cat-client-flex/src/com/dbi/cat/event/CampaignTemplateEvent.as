package com.dbi.cat.event
{
	import com.dbi.cat.common.vo.CampaignVO;
	import com.dbi.cat.common.vo.ConnectorVO;
	import com.dbi.cat.common.vo.NodeVO;
	
	import flash.events.Event;

	public class CampaignTemplateEvent extends Event
	{
		public static const LOAD_CAMPAIGN_TEMPLATE:String = "loadCampaignTemplateEvent";
		public static const DELETE_CAMPAIGN_TEMPLATE:String = "deleteCampaignTemplateEvent";
		public static const SAVE_CAMPAIGN_TEMPLATE:String = "saveCampaignTemplateEvent";
		public static const CLOSE_EDIT_CAMPAIGN_TEMPLATE:String = "closeEditCampaignTemplateEvent";
		public static const CLOSE_CAMPAIGN_TEMPLATE_COMMUNICATIONS:String = "closeCampaignTemplateCommunicationsEvent";
		public static const LIST_CAMPAIGN_TEMPLATES:String = "listCampaignTemplatesEvent";
		public static const EDIT_CAMPAIGN_TEMPLATE:String = "editCampaignTemplateEvent";
		public static const FILTER_CAMPAIGN_TEMPLATES:String = "filterCampaignTemplatesEvent";
	
		public var campaign:CampaignVO;
		public var filterText:String;
			
		public function CampaignTemplateEvent(type:String)
		{
			super(type, true, false);
		}
	}
}