package com.dbi.cat.view.components
{
	import com.dbi.cat.view.workspace.WorkspaceView;
	
	import flash.display.Graphics;
	import flash.events.MouseEvent;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	
	import mx.core.Application;
	import mx.core.UIComponent;

	public class SelectionBox extends UIComponent
	{
		public static const DRAW_OFFSET:Number = 2;
		
		public var isSelecting:Boolean = false;
		private var startX:Number;
		private var startY:Number;
		private var selectRectangle:Rectangle = new Rectangle();
		
		private function get workspace():WorkspaceView
		{
			if (parent != null &&
				parent is WorkspaceView)
				return parent as WorkspaceView;
			return null;
		}
		public function SelectionBox()
		{
			super();
		}
		
		public function startSelection(parentX:Number, parentY:Number):void
		{
			workspace.addEventListener(MouseEvent.MOUSE_MOVE, onMouseMove);
			startX = parentX;
			startY = parentY;
			isSelecting = true;
		}
		public function stopSelection():void
		{
			workspace.removeEventListener(MouseEvent.MOUSE_MOVE, onMouseMove);
			isSelecting = false;
			selectRectangle.setEmpty();
			drawBox();
		}
		public function containsCoordinate(parentPoint:Point):Boolean
		{
			return selectRectangle.contains(parentPoint.x, parentPoint.y);
		}
		private function onMouseMove(e:MouseEvent):void
		{
			drawBox();
		}
		
		private function drawBox():void
		{
			var g:Graphics = graphics;
			g.clear();
			
			if (isSelecting)
			{
				// Use mouse coordinates of the parent
				var currentX:Number = parent.mouseX;
				var currentY:Number = parent.mouseY;
				
				// Adjust selection box so it is always under the mouse
				currentX += startX > currentX ? -DRAW_OFFSET : DRAW_OFFSET;
				currentY += startY > currentY ? -DRAW_OFFSET : DRAW_OFFSET;
				
				selectRectangle.x = startX < currentX ? startX : currentX;
				selectRectangle.y = startY < currentY ? startY : currentY;
				selectRectangle.width = Math.abs(startX - currentX);
				selectRectangle.height = Math.abs(startY - currentY);
				
				g.lineStyle(1, 0x000000);
				g.beginFill(0x7F95FF, 0.3);
				
				g.moveTo(startX, startY);
				g.lineTo(currentX, startY);
				g.lineTo(currentX, currentY);
				g.lineTo(startX, currentY);
				g.lineTo(startX, startY);
				
				g.endFill();
			}
			
		}
	}
}