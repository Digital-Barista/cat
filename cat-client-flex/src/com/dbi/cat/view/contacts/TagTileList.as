package com.dbi.cat.view.contacts
{
	import flash.events.Event;
	
	import mx.controls.TileList;
	import mx.events.CollectionEvent;

	public class TagTileList extends TileList
	{
		public function TagTileList()
		{
			super();
		}

		override public function set rowHeight(value:Number):void
		{
			super.rowHeight = value;
			updateHeight();
		}		
		override public function set dataProvider(value:Object):void
		{
			super.dataProvider = value;
			if (dataProvider != null)
				dataProvider.addEventListener(CollectionEvent.COLLECTION_CHANGE, updateHeight);
			updateHeight();
		}
		private function updateHeight(e:Event=null):void
		{
			if (dataProvider != null)
				height = rowHeight * Math.ceil(dataProvider.length / columnCount);
		}
	}
}