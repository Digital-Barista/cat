package com.dbi.controls
{
	import mx.controls.Image;
	import mx.controls.dataGridClasses.DataGridListData;

	public class CheckMarkItemRenderer extends Image
	{
		[Embed("/assets/images/check_mark.png")]
		public var trueIcon:Class;
		
			
		public function CheckMarkItemRenderer()
		{
			super();
			
			source = trueIcon;
			percentHeight = 100;
			percentWidth = 100;
			scaleContent = false;
			setStyle("horizontalAlign", "center");
			setStyle("verticalAlign", "middle");
		}
		
		protected override function updateDisplayList(w:Number, h:Number):void
		{
			super.updateDisplayList(w, h);
			
			if (listData != null &&
				data != null &&
				data.hasOwnProperty(DataGridListData(listData).dataField) &&
				data[DataGridListData(listData).dataField])
			{
				source = trueIcon;
			}
			else
			{
				source = null;
			}
		}
	}
}