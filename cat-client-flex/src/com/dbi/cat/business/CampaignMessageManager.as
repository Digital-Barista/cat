package com.dbi.cat.business
{
	import com.dbi.cat.view.workspace.MessagePreviewView;
	
	import flash.display.DisplayObject;
	
	import mx.collections.ArrayCollection;
	import mx.core.Application;
	import mx.core.IFlexDisplayObject;
	import mx.managers.PopUpManager;
	
	[Bindable]
	public class CampaignMessageManager
	{
		public var messageParts:ArrayCollection;
		
		private var messagePreviewWindow:IFlexDisplayObject;
		
		public function CampaignMessageManager()
		{
		}

		public function loadMessageParts(parts:ArrayCollection):void
		{
			messageParts = parts;
		}
		
		public function openMessagePreview():void
		{
			messageParts = null;
			
			if (messagePreviewWindow == null)
			{
				messagePreviewWindow = new MessagePreviewView();
			}
			
			PopUpManager.addPopUp(messagePreviewWindow, DisplayObject(Application.application), true);
			PopUpManager.centerPopUp(messagePreviewWindow);
		}
		
		public function closeMessagePreview():void
		{
			PopUpManager.removePopUp(messagePreviewWindow);
		}
	}
}