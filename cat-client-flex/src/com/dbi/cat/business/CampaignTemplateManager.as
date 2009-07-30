package com.dbi.cat.business
{
	import com.dbi.cat.common.vo.CampaignVO;
	import com.dbi.cat.view.campaign.EditCampaignTemplateView;
	
	import flash.display.DisplayObject;
	
	import mx.collections.ArrayCollection;
	import mx.core.Application;
	import mx.core.IFlexDisplayObject;
	import mx.managers.PopUpManager;
	import mx.utils.ObjectUtil;
	
	[Bindable]
	public class CampaignTemplateManager
	{
		public var campaignTemplateList:ArrayCollection = new ArrayCollection();
		private var editCampaignTemplatePopup:IFlexDisplayObject;
		
		public function CampaignTemplateManager()
		{
		}
		
		//
		// Update methods
		//
		public function editCampaignTemplate(campaign:CampaignVO):void
		{
			// Create view
			if (editCampaignTemplatePopup == null)
			{
				editCampaignTemplatePopup = new EditCampaignTemplateView();
			}
			else
			{
				closeEditCampaignTemplate();
			}
					
			EditCampaignTemplateView(editCampaignTemplatePopup).campaign = ObjectUtil.copy(campaign) as CampaignVO;	
				
			// Add popup to application
			PopUpManager.addPopUp(editCampaignTemplatePopup, DisplayObject(Application.application), true);
			PopUpManager.centerPopUp(editCampaignTemplatePopup);
		}

		//
		// Close popup methods
		//
		public function closeEditCampaignTemplate():void
		{
			PopUpManager.removePopUp(editCampaignTemplatePopup);
		}
	}
}