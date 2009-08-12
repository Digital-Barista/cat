package com.dbi.cat.view.components
{
	import flash.events.Event;
	
	import mx.controls.TextArea;

	public class DynamicTextArea extends TextArea
	{
	    public function DynamicTextArea()
	    {
			super();
			horizontalScrollPolicy = "off";
			verticalScrollPolicy = "off";
			addEventListener(Event.CHANGE, adjustSizeHandler);
	    }
	    
		private function adjustSizeHandler(event:Event):void
		{
			if(height <= textField.textHeight + textField.getLineMetrics(0).height)
			{
				height = textField.textHeight;		 
				validateNow();
			}
			if(width <= textField.textWidth + textField.getLineMetrics(0).width)
			{
				width = textField.textWidth;		 
				validateNow();
			}
		}
			
		private function validateSizes():void
		{
			textField.width = maxWidth;
			validateNow();
			width = textField.textWidth;
			height = textField.textHeight;
			validateNow();
		}
		override public function set text(val:String):void
		{
			textField.text = val;
			validateSizes();
		}
		override public function set htmlText(value:String):void
		{
			textField.htmlText = value;
			validateSizes();
		}
		
		override public function set width(value:Number):void
		{
			if (textField == null)
			{
				if (width <= value)
					super.width = value;
			}
			else
			{
				var currentWidth:uint = textField.getLineMetrics(0).width; 
				if (currentWidth <= maxWidth)
				{
					super.width = currentWidth;
				}
				else
				{
					super.width = maxWidth;
				}
			}
		}
		override public function set height(value:Number):void
		{
			if(textField == null)
			{
				if(height <= value)
					super.height = value;
			}
			else
			{			 
				var currentHeight:uint = textField.textHeight + textField.getLineMetrics(0).height/2;
				if (currentHeight <= maxHeight)
				{
					super.height = currentHeight;
				}
				else
				{
					super.height = maxHeight;				 
				}	
			}
		}
		override public function get text():String
		{
			return textField.text;
		}
		override public function get htmlText():String
		{
			return textField.text;
		}
	}
}