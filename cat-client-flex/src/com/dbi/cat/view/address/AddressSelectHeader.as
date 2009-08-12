package com.dbi.cat.view.address
{
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.containers.HBox;
	import mx.controls.Image;

	public class AddressSelectHeader extends HBox
	{
		
		[Embed("/assets/img/selectAll.png")]
		private var selectAllIcon:Class;
		
		[Embed("/assets/img/unselectAll.png")]
		private var unselectAllIcon:Class;
		
		private var selectAll:Image;
		private var unselectAll:Image;
		
		public function AddressSelectHeader()
		{
			super();
			
			percentWidth = 100;
			setStyle("horizontalAlign", "center");
			setStyle("verticalAlign", "middle");
			
			selectAll = new Image();
			selectAll.source = selectAllIcon;
			selectAll.addEventListener(MouseEvent.CLICK, onSelectAll);
			addChild(selectAll);
			
			unselectAll = new Image();
			unselectAll.source = unselectAllIcon;
			unselectAll.addEventListener(MouseEvent.CLICK, onUnselectAll);
			addChild(unselectAll);
		}
		
		private function onSelectAll(e:MouseEvent):void
		{
			dispatchEvent(new Event("selectAll", true));
		}
		private function onUnselectAll(e:MouseEvent):void
		{
			dispatchEvent(new Event("unselectAll", true));
		}
		
	}
	
}