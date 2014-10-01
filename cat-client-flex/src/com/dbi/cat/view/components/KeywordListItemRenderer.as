package com.dbi.cat.view.components
{
	import com.dbi.cat.view.workspace.WorkspaceItem;
	import com.dbi.cat.view.workspace.WorkspaceView;
	
	import mx.controls.Label;
	
	public class KeywordListItemRenderer extends Label
	{
		private var _enabled:Boolean = true;
		
		public function KeywordListItemRenderer()
		{
		}
		
		override public function set data(value:Object):void
		{
			var workspace:WorkspaceView;
			var p:Object = parentDocument;
			while(p != null &&
				workspace == null)
			{
				if (p is WorkspaceView)
					workspace = p as WorkspaceView;
				p = p.parent;
			}
			
			_enabled = value == null ||
				value.campaignUID == null || 
				workspace == null ||
				workspace.campaign == null ||
				value.campaignUID == workspace.campaign.uid;
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