package com.dbi.cat.view.components
{
	import com.dbi.cat.view.workspace.Node;
	import com.dbi.cat.view.workspace.WorkspaceItem;
	
	import mx.controls.Label;
	
	public class KeywordListItemRenderer extends Label
	{
		private var _enabled:Boolean = true;
		
		public function KeywordListItemRenderer()
		{
		}
		
		override public function set data(value:Object):void
		{
			var item:WorkspaceItem = parentDocument as WorkspaceItem;
			
			_enabled = value == null ||
				value.campaignUID == null || 
				item == null ||
				item.workspace == null ||
				item.workspace.campaign == null ||
				value.campaignUID == item.workspace.campaign.uid;
			super.data = value;
		}    

           
        override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void
        {
			super.updateDisplayList(unscaledWidth, unscaledHeight);
			
			if (!this._enabled) 
			{
				textField.setColor(getStyle("disabledColor"));
			}
			else
			{
				textField.setColor(getStyle("color"));
			}
        } 
	}
}