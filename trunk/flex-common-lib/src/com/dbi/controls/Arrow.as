package com.dbi.controls
{
	import flash.display.Graphics;
	import flash.geom.Point;
	
	import mx.core.UIComponent;
	import mx.styles.CSSStyleDeclaration;
	import mx.styles.StyleManager;
	
	/**
	 * Determines the fill color of the triangle that is drawn
	 */
	[Style(name="fillColor", type="uint", format="Color", inherit="yes")]

	/**
	 * The Arrow is a UIComponent that draws a triangle in one of four allowed
	 * directions: up, down, left, right.
	 * 
	 * @author khoyt2
	 * 
	 */
	public class Arrow extends UIComponent
	{
		private static const DEFAULT_WIDTH:Number = 10;
		private static const DEFAULT_HEIGHT:Number = 5;
		
		private var _direction:String = "down";
		
		[Inspectable(enumeration="up,down,left,right")]
		/**
		 * Returns the current direction the arrow is pointing
		 * 
		 * @return String value representing the direction
		 */
		public function get direction():String
		{
			return _direction;
		}
		
		/**
		 * Set the direction of the arrow and redraw it
		 * 
		 * @param value The direction the arrow should point
		 */
		public function set direction(value:String):void
		{
			_direction = value;
			drawArrow();
		}
		
        private static var classConstructed:Boolean = classConstruct();
    
        // Define a static method.
        private static function classConstruct():Boolean 
        {
            if (!StyleManager.getStyleDeclaration("Arrow"))
            {
                // If there is no CSS definition for Arrow, 
                // then create one and set the default value.
                var arrow:CSSStyleDeclaration = new CSSStyleDeclaration();
                arrow.defaultFactory = function():void
                {
                    this.fillColor = 0x0;
                }
                StyleManager.setStyleDeclaration("Arrow", arrow, true);

            }
            return true;
        }

		public function Arrow()
		{
			super();
		}
		
		/**
		 * Override to set default width and height
		 */
		protected override function measure():void
		{
			super.measure();
			
			measuredWidth = DEFAULT_WIDTH;
			measuredHeight = DEFAULT_HEIGHT;
		}
		
		/**
		 * Override to force a redraw when the display list is updated
		 */
		protected override function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void
		{
			super.updateDisplayList(unscaledWidth, unscaledHeight);
			drawArrow();
		}
		
		/**
		 * Draw the arrow in the set direction
		 */
		private function drawArrow():void
		{
			var g:Graphics = graphics;
			g.clear();
			
			// Get the arrow fill color
			var fillColor:Number = getStyle("fillColor");
		
			g.lineStyle(1, 0, 0);
			g.beginFill(fillColor, 1);
			
			// Arrow down
			var p1:Point = new Point(0, 0);
			var p2:Point = new Point(width, 0);
			var p3:Point = new Point(width/2, height);
			
			if (direction == "up")
			{
				p1 = new Point(0, height);
				p2 = new Point(width/2, 0);
				p3 = new Point(width, height);
			}
			else if (direction == "right")
			{
				p1 = new Point(0, 0);
				p2 = new Point(width, height/2);
				p3 = new Point(0, height);
			}
			else if (direction == "left")
			{
				p1 = new Point(width, 0);
				p2 = new Point(0, height/2);
				p3 = new Point(width, height);
			}
			
			
			g.moveTo(p1.x, p1.y);
			g.lineTo(p2.x, p2.y);
			g.lineTo(p3.x, p3.y);
			g.lineTo(p1.x, p1.y);
			g.endFill();
		}
	}
}