package com.dbi.cat.business
{
	import com.dbi.cat.common.vo.CampaignVO;
	import com.dbi.cat.view.EditTemplateCommunicationsView;
	import com.dbi.cat.view.campaign.EditCampaignTemplateView;
	
	import flash.display.DisplayObject;
	
	import mx.collections.ArrayCollection;
	import mx.core.Application;
	import mx.core.IFlexDisplayObject;
	import mx.core.UIComponent;
	import mx.managers.PopUpManager;
	import mx.utils.ObjectUtil;
	
	[Bindable]
	public class CampaignTemplateManager
	{
		public var campaignTemplateList:ArrayCollection = new ArrayCollection();
		public var campaignTemplate:CampaignVO;
		
		private var filterText:String;
		
		private var editCampaignTemplatePopup:IFlexDisplayObject;
		private var editCampaignTemplateCommunicationPopup:IFlexDisplayObject;
		
		public function CampaignTemplateManager()
		{
		}
		
		//
		// Update methods
		//
		public function loadCampaignTemplate(campaign:CampaignVO):void
		{
			campaignTemplate = campaign;
			
			// Move to communication screen if not open
			if (editCampaignTemplateCommunicationPopup == null)
			{
				editCampaignTemplateCommunicationPopup = new EditTemplateCommunicationsView();
				editCampaignTemplateCommunicationPopup.width = Application.application.width;
				editCampaignTemplateCommunicationPopup.height = Application.application.height;
			}
			PopUpManager.removePopUp(editCampaignTemplateCommunicationPopup);
			PopUpManager.addPopUp(editCampaignTemplateCommunicationPopup, UIComponent(Application.application), true);	
			PopUpManager.centerPopUp(editCampaignTemplateCommunicationPopup);
		}
		
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
		public function closeCampaignTemplateCommunications():void
		{
			campaignTemplate = null;
			PopUpManager.removePopUp(editCampaignTemplateCommunicationPopup);
		}
		
		
		//
		// Filter methods
		//
		public function filterTemplates(filterText:String):void
		{
			this.filterText = filterText;
			if (campaignTemplateList != null)
			{
				campaignTemplateList.filterFunction = filterTemplatesFunction;
				campaignTemplateList.refresh();
			}
		}
		private function filterTemplatesFunction(campaign:CampaignVO):Boolean
		{
			if (filterText == null ||
				campaign.name.toLowerCase().indexOf(filterText.toLowerCase()) > -1)
				return true;
			return false;
		}
	}
}