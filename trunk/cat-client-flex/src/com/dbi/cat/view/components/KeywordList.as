package com.dbi.cat.view.components
{
	import com.dbi.cat.view.workspace.WorkspaceView;
	
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	
	import mx.controls.List;
	import mx.controls.listClasses.IListItemRenderer;
	import mx.core.ClassFactory;

	public class KeywordList extends List
	{
		public function KeywordList()
		{
			super();
			this.itemRenderer = new ClassFactory(KeywordListItemRenderer);
		}

		/**
		* Prevent mouse over handler when item is disabled.
		*/
		override protected function mouseOverHandler(event:MouseEvent):void
		{
			var item:IListItemRenderer = mouseEventToItemRenderer(event);
			
			if (!itemDisable(event)) 
				super.mouseOverHandler(event);
		}



		/**
		* Prevent mouse down handler when item is disabled.
		*/        
		override protected function mouseDownHandler(event:MouseEvent):void 
		{
			if (itemDisable(event)) 
				return;
			else
				super.mouseDownHandler(event);
		}



		/**
		* Prevent mouse up handler when item is disabled.
		*/        
		override protected function mouseUpHandler(event:MouseEvent):void 
		{
			if (itemDisable(event))
				return;
			else
				super.mouseUpHandler(event);
		}



		/**
		* Prevent mouse click handler when item is disabled.
		*/        
		override protected function mouseClickHandler(event:MouseEvent):void 
		{
			if (itemDisable(event))
				return;
			else
				super.mouseClickHandler(event);
		}



		/**
		* Prevent mouse double click handler when item is disabled.
		*/        
		override protected function mouseDoubleClickHandler(event:MouseEvent):void 
		{
			if (itemDisable(event))
				event.preventDefault();
			else
				super.mouseDoubleClickHandler(event);
		}
		
		
		
		/**
		* Prevent mouse double click handler when item is disabled.
		*/       
		override protected function keyDownHandler(event:KeyboardEvent):void 
		{
			event.stopPropagation();
		} 
		
		private function itemDisable(event:MouseEvent):Boolean 
		{
			var item:IListItemRenderer = mouseEventToItemRenderer(event);
			var workspace:WorkspaceView;
			var p:Object = parentDocument;
			while(p != null &&
				workspace == null)
			{
				if (p is WorkspaceView)
					workspace = p as WorkspaceView;
				p = p.parent;
			}
			
			if (item != null &&
				item.data != null &&
				item.data.campaignUID != null && 
				workspace != null &&
				workspace.campaign != null &&
				item.data.campaignUID != workspace.campaign.uid)
				return true;
				
			return false;
		}
	}
}