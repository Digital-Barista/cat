package com.dbi.cat.view.contacts
{
	import com.dbi.cat.common.vo.ContactTagVO;
	
	import mx.containers.HBox;
	import mx.controls.CheckBox;
	import mx.controls.Image;
	import mx.controls.Label;

	public class TagItemRenderer extends HBox
	{
		[Embed("/assets/swf/delete.swf")]
		private var deleteIcon:Class;
		
		private var hbox:HBox;
		private var check:CheckBox;
		private var tag:Label;
		private var remove:Image;
			
		public function TagItemRenderer()
		{
			percentWidth = 100;
			super();
		}
		
     
        override protected function updateDisplayList(w:Number, h:Number):void
        {
        	super.updateDisplayList(w, h);
				
			if (check == null)
			{
				check = new CheckBox();
				check.setStyle("paddingLeft", 20);
				addChild(check);
			}
			
			if (tag == null)
        	{
        		tag = new Label();
        		tag.percentWidth = 100;
				addChild(tag);
        	}
			
			if (remove == null)
			{
				remove = new Image();
				remove.source = deleteIcon;
				remove.width = 18;
				remove.height = 18;
				addChild(remove);
			}
        	
        	if (data)
        	{
	            var isTag:Boolean = data is ContactTagVO
	            
	            check.visible = isTag;
	            remove.visible = isTag;
	            
	            if (tag)
					tag.text = data.tag;
	        }
        }

		
	}
}