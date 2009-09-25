package com.dbi.cat.view.components
{
	import mx.controls.treeClasses.TreeItemRenderer;
	import mx.controls.treeClasses.TreeListData;

	public class ResizeableIconTreeItemRenderer extends TreeItemRenderer
	{
		public var iconWidth:Number = 18;
		public var iconHeight:Number = 18;
		
		public function ResizeableIconTreeItemRenderer()
		{
			super();
		}
		
		override protected function measure():void
		{
			super.measure();
	
			var w:Number = data ? TreeListData(listData).indent : 0;
	
			if (disclosureIcon)
				w += disclosureIcon.width;
	
			if (icon)
				w += icon.measuredWidth;
	
			// guarantee that label width isn't zero because it messes up ability to measure
			if (label.width < 4 || label.height < 4)
			{
				label.width = 4;
				label.height = 16;
			}
	
			if (isNaN(explicitWidth))
			{
				w += label.getExplicitOrMeasuredWidth();	
				measuredWidth = w;
				measuredHeight = label.getExplicitOrMeasuredHeight();
			}
			else
			{
				label.width = Math.max(explicitWidth - w, 4);
				measuredHeight = label.getExplicitOrMeasuredHeight();
				if (icon && iconHeight > measuredHeight)
					measuredHeight = iconHeight;
			}
		}
	
		override protected function updateDisplayList(unscaledWidth:Number,
												  unscaledHeight:Number):void
		{
			super.updateDisplayList(unscaledWidth, unscaledHeight);
			
			var startx:Number = data ? TreeListData(listData).indent : 0;
		
			if (disclosureIcon)
			{
				startx = disclosureIcon.x + disclosureIcon.width;
			}
			
			if (icon)
			{
				icon.x = startx;
				startx = icon.x + iconWidth;
				icon.setActualSize(iconWidth, iconHeight);
			}
			
			label.x = startx;
			label.setActualSize(unscaledWidth - startx, measuredHeight);
	
			var verticalAlign:String = getStyle("verticalAlign");
			if (verticalAlign == "top")
			{
				label.y = 0;
				if (icon)
					icon.y = 0;
				if (disclosureIcon)
					disclosureIcon.y = 0;
			}
			else if (verticalAlign == "bottom")
			{
				label.y = unscaledHeight - label.height + 2; // 2 for gutter
				if (icon)
					icon.y = unscaledHeight - iconHeight;
				if (disclosureIcon)
					disclosureIcon.y = unscaledHeight - disclosureIcon.height;
			}
			else
			{
				label.y = (unscaledHeight - label.height) / 2;
				if (icon)
					icon.y = (unscaledHeight - iconHeight) / 2;
				if (disclosureIcon)
					disclosureIcon.y = (unscaledHeight - disclosureIcon.height) / 2;
			}
		}
	}
}