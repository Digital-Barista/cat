package com.dbi.cat.event
{
	import com.dbi.cat.common.constants.EntryPointType;
	import com.dbi.cat.common.vo.CampaignVO;
	import com.dbi.cat.common.vo.CouponNodeVO;
	import com.dbi.cat.common.vo.MessageVO;
	import com.dbi.cat.common.vo.criteria.ContactSearchCriteriaVO;
	
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;

	public class EntryMessageCampaignEvent extends Event
	{
      public static const LOAD_ENTRY_MESSAGE_CAMPAIGN:String = "loadEntryMessageEvent";
		public static const SAVE_ENTRY_MESSAGE_CAMPAIGN:String = "saveEntryMessageEvent";
		
		public var campaign:CampaignVO;
			
		public function EntryMessageCampaignEvent(type:String)
		{
			super(type, true, false);
		}
	}
}