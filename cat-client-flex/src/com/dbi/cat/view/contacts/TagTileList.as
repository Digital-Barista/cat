package com.dbi.cat.view.contacts
{
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;
	import mx.controls.TileList;
	import mx.events.CollectionEvent;

	[Event(name="checkboxChanged", type="flash.events.Event")]
	
	public class TagTileList extends TileList
	{
		private var _selectedTags:ArrayCollection;
		public function get selectedTags():ArrayCollection
		{
			return _selectedTags;
		}
		public function set selectedTags(value:ArrayCollection):void
		{
			if (_selectedTags != null)
				_selectedTags.removeEventListener(CollectionEvent.COLLECTION_CHANGE, tagsChanged);
			_selectedTags = value;
			if (_selectedTags)
				_selectedTags.addEventListener(CollectionEvent.COLLECTION_CHANGE, tagsChanged);
		}
		
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
		
		private function tagsChanged(e:Event):void
		{
			invalidateList();
		}
	}
}