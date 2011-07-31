package com.dbi.cat.business
{
    import com.dbi.cat.common.vo.CampaignVO;

    public class EntryMessageCampaignManager
    {
        [Bindable]
        public var currentEntryCampaign:CampaignVO;
        
        public function EntryMessageCampaignManager()
        {
        }
        
        public function loadEntryMessageCampaign(campaign:CampaignVO):void
        {
            currentEntryCampaign = campaign;
        }
        
        public function saveEntryMessageCampaign(campaign:CampaignVO):void
        {
            currentEntryCampaign = campaign;
        }
    }
}