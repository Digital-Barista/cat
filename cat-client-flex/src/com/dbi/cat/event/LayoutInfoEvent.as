package com.dbi.cat.event
{
	import com.dbi.cat.common.vo.CampaignVO;
	import com.dbi.cat.common.vo.LayoutInfoVO;
	
	import flash.events.Event;

	public class LayoutInfoEvent extends Event
	{
		public static const LOAD_CAMPAIGN_LAYOUT_INFO:String = "loadCampaignLayoutInfoEvent";
		public static const SAVE_LAYOUT_INFO:String = "saveLayoutInfoEvent";
		public static const SYNC_LAYOUT_INFO:String = "syncLayoutInfoEvent";
		public static const LAYOUT_LOADED:String = "layoutLoadedEvent";
		
		public var campaign:CampaignVO;
		public var layoutInfo:LayoutInfoVO;
		
		public function LayoutInfoEvent(type:String)
		{
			super(type, true, false);
		}
		
	}
}