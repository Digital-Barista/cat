package com.dbi.cat.view.contacts
{
	import com.dbi.cat.common.constants.ContactTagType;
	import com.dbi.cat.common.vo.ContactTagVO;
	import com.dbi.cat.event.ContactEvent;
	
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.containers.HBox;
	import mx.controls.CheckBox;
	import mx.controls.Image;
	import mx.controls.Label;
	import mx.events.ListEvent;

	public class TagItemRenderer extends HBox
	{
		[Event(name="checkboxChanged", type="flash.events.Event")]
	
		[Embed("/assets/swf/delete.swf")]
		private var deleteIcon:Class;
		
		private var count:Label;
		private var tag:Label;
		private var remove:Image;
		private var check:CheckBox;
			
		public function get selected():Boolean
		{
			return check != null && check.selected;
		}
		
		public function TagItemRenderer()
		{
			horizontalScrollPolicy = "off";
			percentWidth = 100;
			super();
		}
		
     
        override protected function updateDisplayList(w:Number, h:Number):void
        {
        	super.updateDisplayList(w, h);
				
			if (check == null)
			{
				check = new CheckBox();
				check.addEventListener(Event.CHANGE, onCheckChanged);
				addChild(check);
			}
			
			if (count == null)
			{
				count = new Label();
//				addChild(count);
			}
			
			if (tag == null)
        	{
        		tag = new Label();
        		tag.setStyle("textAlign", "left");
        		tag.percentWidth = 100;
				addChild(tag);
        	}
			
			if (remove == null)
			{
				remove = new Image();
				remove.source = deleteIcon;
				remove.width = 18;
				remove.height = 18;
				remove.addEventListener(MouseEvent.CLICK, onRemoveClick);
//				addChild(remove);
			}
        	
        	if (data)
        	{
        		remove.visible = data.type == ContactTagType.USER;
	            count.text = "(" + data.contactCount + ")";
				tag.text = data.tag;
				
				var list:TagTileList = owner as TagTileList;
				check.selected = list != null && 
					list.selectedTags != null && 
					list.selectedTags.contains(data);
	        }
        }
        
        private function onCheckChanged(e:Event):void
        {
        	var event:Event = new Event("checkboxChanged", true);
        	dispatchEvent(event);
        }
        private function onRemoveClick(e:MouseEvent):void
        {
        	var event:ContactEvent = new ContactEvent(ContactEvent.DELETE_CONTACT_TAG);
        	event.contactTag = data as ContactTagVO;
        	dispatchEvent(event);
        }

		
	}
}