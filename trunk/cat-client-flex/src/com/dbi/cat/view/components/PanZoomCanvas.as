package com.dbi.cat.view.components
{
	import flash.display.DisplayObject;
	import flash.display.Sprite;
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.geom.Point;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.containers.Canvas;
	import mx.effects.AnimateProperty;
	import mx.effects.Move;
	import mx.effects.Parallel;

	public class PanZoomCanvas extends Canvas
	{
		public static const ZOOM_FACTOR_INTERVAL:Number = 10;
		public static const CONTAINER_DIMENSION:Number = 100;
		
		public static const MODE_PAN:String = "pan";
		public static const MODE_ZOOM_IN:String = "zoomIn";
		public static const MODE_ZOOM_OUT:String = "zoomOut";
		
		private var centerMove:Move;
		private var zoomFactor:Number = 100;
		private var contentContainer:Canvas;
		private var dragTarget:Sprite;
		private var parallel:Parallel;
		
		public var mode:String = MODE_PAN;
		
        [Embed("/assets/swf/hand.swf")]
        public static const hand:Class;
		
        [Embed("/assets/swf/hand-closed.swf")]
        public static const handClosed:Class;
		
		private var firstLoad:Boolean = true;
	        
		public function PanZoomCanvas()
		{
			super();
			
			contentContainer = new Canvas();
			horizontalScrollPolicy = "off";
			verticalScrollPolicy = "off";
			doubleClickEnabled = true;
			
			addEventListener(MouseEvent.MOUSE_WHEEL, onScroll);
			addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
			addEventListener(MouseEvent.MOUSE_UP, onMouseUp);
			addEventListener(MouseEvent.CLICK, onMouseClick);
		}
  		
  		override public function get mouseX():Number
  		{
  			return contentContainer.mouseX;
  		}
  		override public function get mouseY():Number
  		{
  			return contentContainer.mouseY;
  		}
		
		override protected function childrenCreated():void
		{
			super.childrenCreated();
			
			contentContainer.width = CONTAINER_DIMENSION;
			contentContainer.height = CONTAINER_DIMENSION;
			contentContainer.clipContent = false;
			contentContainer.addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown, false, 0, true);
			contentContainer.addEventListener(MouseEvent.MOUSE_UP, onMouseUp, false, 0, true);
			contentContainer.addEventListener(MouseEvent.CLICK, onMouseClick, false, 0, true);
			super.addChildAt(contentContainer, 0);
		}
		
		public function addComponent(component:DisplayObject):void
		{
			contentContainer.addChild(component);
		}
		public function addComponentAtBottom(component:DisplayObject):void
		{
			contentContainer.addChildAt(component, 0);
		}
		public function removeComponent(component:DisplayObject):void
		{
			contentContainer.removeChild(component);
		}
		public function getComponents():Array
		{
			return contentContainer.getChildren();
		}
		public function removeAllComponents():void
		{
			contentContainer.removeAllChildren();
		}
		
		public function changeZoom(zoomFactor:Number):void
		{
			contentContainer.scaleX = zoomFactor;
			contentContainer.scaleY = zoomFactor;
			
			this.zoomFactor = zoomFactor * 100;
		}
		public function changePan(point:Point):void
		{
			contentContainer.x = -point.x;
			contentContainer.y = -point.y;
		}
		public function fitComponents():void
		{
			if (getComponents().length > 0)
			{
				var minX:Number;
				var maxX:Number;
				var minY:Number;
				var maxY:Number;
				
				for each (var child:DisplayObject in getComponents())
				{
					if (isNaN(maxX) ||
						child.x + child.width > maxX)
						maxX = child.x + child.width;
					if (isNaN(minX) ||
						child.x < minX)
						minX = child.x;
						
					if (isNaN(maxY) || 
						child.y + child.height > maxY)
						maxY = child.y + child.height;
					if (isNaN(minY) ||
						child.y < minY)
						minY = child.y;
				}
				
				// Get the zoom factor according to the necessary size to show
				// all components compared to the original size of the zoom container (100px)
				var newWidth:Number = maxX - minX;
				var newHeight:Number = maxY - minY;
				var factor:Number = width / newWidth;
				if (height / newHeight < factor)
					factor = height / newHeight;
				factor = Math.floor(factor * 10) / 10;
				
				// Reset factor to 1 if it is below zero or above 1
				if (factor <= 0 ||
					factor > 1)
					factor = 1;
				zoomFactor = factor * 100;
				
				// Animate change
				var move:Move = new Move(contentContainer);
				move.xTo = (-minX * factor) + (width - newWidth*factor)/2;
				move.yTo = (-minY * factor) + (height - newHeight*factor)/2;
				
				var zoomX:AnimateProperty = new AnimateProperty(contentContainer);
				zoomX.property = "scaleX";
				zoomX.toValue = factor;
				
				var zoomY:AnimateProperty = new AnimateProperty(contentContainer);
				zoomY.property = "scaleY";
				zoomY.toValue = factor;
				
				if (parallel != null &&
					parallel.isPlaying)
					parallel.stop();
					
				parallel = new Parallel()
				parallel.addChild(move);
				parallel.addChild(zoomX);
				parallel.addChild(zoomY);
				
				parallel.play();
			}
		}
		
		private function onMouseClick(e:MouseEvent):void
		{
			if (mode == MODE_ZOOM_IN)
			{
				doZoom(true);
			}
			else if (mode == MODE_ZOOM_OUT)
			{
				doZoom(false);
			}
		}
		private function onMouseDown(e:MouseEvent):void
		{
			// Only perform an action if nothing is currently being dragged
			if (!e.shiftKey &&
				dragTarget == null &&
				(e.target == this ||
				e.target == contentContainer))
			{
				if (mode == MODE_PAN)
				{
	    			cursorManager.setCursor(handClosed);
					dragTarget = e.currentTarget as Sprite;
					
					// If trying to drag this component drag the content container instead
					if (dragTarget == this)
						dragTarget = contentContainer;
					dragTarget.startDrag(false);
				}
				
			}
		}
		
		private function onMouseUp(e:MouseEvent):void
		{
			if (dragTarget != null)
			{
				dragTarget.stopDrag();
				dragTarget = null;
			}
    		cursorManager.removeAllCursors();
		}
		
		private function onScroll(e:MouseEvent):void
		{
			doZoom(e.delta > 0);
		}
		
		public function doZoom(zoomIn:Boolean, zoomPoint:Point=null):void
		{
			if (zoomIn)
			{
				zoomFactor += ZOOM_FACTOR_INTERVAL;
			}
			else if (zoomFactor > ZOOM_FACTOR_INTERVAL)
			{
				// Don't let zoomFactor get lower than the zoom interval
				if (zoomFactor - ZOOM_FACTOR_INTERVAL > ZOOM_FACTOR_INTERVAL)
					zoomFactor -= ZOOM_FACTOR_INTERVAL;
			}
			
			var percentZoom:Number = zoomFactor/100;
			
			if (zoomPoint == null)
				zoomPoint = new Point(mouseX, mouseY);
			focusZoom(contentContainer.scaleX, percentZoom, zoomPoint);
			
			changeZoom(percentZoom);
		}

		private function focusZoom(previousZoom:Number, newZoom:Number, zoomPoint:Point):void
		{
			//Move container so the zoom will foucus on the mouse point
			var dX:Number = ((zoomPoint.x / newZoom) - zoomPoint.x) -
							((zoomPoint.x / previousZoom) - zoomPoint.x) * (previousZoom / newZoom) ;
							
			var dY:Number = ((zoomPoint.y / newZoom) - zoomPoint.y) -
							((zoomPoint.y / previousZoom) - zoomPoint.y) * (previousZoom / newZoom) ;
			
	
			contentContainer.x += dX * newZoom;
			contentContainer.y += dY * newZoom;	
		}
		
	}
}