package com.dbi.cat.business
{
    import com.dbi.cat.common.vo.CampaignEntryMessageVO;
    import com.dbi.cat.common.vo.CampaignVO;
    import com.dbi.cat.common.vo.MessageVO;

    public class EntryMessageCampaignManager
    {
        [Bindable]
        public var currentCampaignEntryMessage:CampaignEntryMessageVO;
        
        public function EntryMessageCampaignManager()
        {
        }
        
        public function loadEntryMessageCampaign(campaignEntryMessage:CampaignEntryMessageVO):void
        {
			currentCampaignEntryMessage = campaignEntryMessage;
        }
        
        public function saveEntryMessageCampaign(campaignEntryMessage:CampaignEntryMessageVO):void
        {
			currentCampaignEntryMessage = campaignEntryMessage;
        }
    }
}