package com.dbi.controls
{
	
	import com.dbi.event.CustomMessageEvent;
	
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.text.TextField;
	import flash.ui.Keyboard;
	
	import mx.binding.utils.BindingUtils;
	import mx.containers.HBox;
	import mx.containers.TitleWindow;
	import mx.controls.Button;
	import mx.controls.TextArea;
	import mx.core.Application;
	import mx.core.UIComponent;
	import mx.events.FlexEvent;
	import mx.managers.PopUpManager;

	[Bindable]
	public class CustomMessage extends TitleWindow
	{
			
		private static var messageInstance:Object = new Object();
		
		public static var minWindowHeight:Number = 100;
		public static var minWindowWidth:Number = 150;
		public static var maxWindowWidth:Number = 500;
		
		
		private var _text:String;
		public function get text():String
		{
			return _text;
		}
		public function set text(value:String):void
		{
			_text = value;
			TextContainer.htmlText = value;
		}
		
		private var _buttons:Array = ["OK"];
		public function get buttons():Array
		{
			return _buttons;
		}
		public function set buttons(names:Array):void
		{
			_buttons = names;
			createButtons();
		}
		
		// Layout controls
		public var TextContainer:TextArea;
		public var ButtonContainer:HBox;
		
		/**
		 * Callback function to be fired when a button is clicked on the message popup
		 */
		public var buttonClick:Function;
		
		/**
		 * Constructor
		 */
		public function CustomMessage()
		{
			super();
			
			horizontalScrollPolicy = "off";
			verticalScrollPolicy = "off";
			
			setStyle("styleName", "customMessage");
			layout = "absolute";
			
			TextContainer = new TextArea();
			TextContainer.editable = false;
			TextContainer.wordWrap = true;
			TextContainer.tabEnabled = false;
			TextContainer.setStyle("textAlign", "center");
			TextContainer.setStyle("borderStyle", "none");
			TextContainer.setStyle("fontSize", 12);
			TextContainer.setStyle("right", 10);
			TextContainer.setStyle("left", 10);
			TextContainer.setStyle("top", 10);
			TextContainer.setStyle("bottom", 50);
      TextContainer.setStyle("backgroundAlpha", 0);
			BindingUtils.bindProperty(TextContainer, "htmlText", this, "text");
			addChild(TextContainer);
			
			ButtonContainer = new HBox();
			ButtonContainer.setStyle("horizontalAlign", "center");
			ButtonContainer.setStyle("right", 0);
			ButtonContainer.setStyle("left", 0);
			ButtonContainer.setStyle("bottom", 10);
			addChild(ButtonContainer);
			
			createButtons();
		}
		
		/**
		 * Resize the canvas to fit the message when the display list is updated
		 */
		override protected function updateDisplayList(w:Number, h:Number):void
		{
			super.updateDisplayList(w, h);
			resize();
		}
		
		private function createButtons():void
		{
			ButtonContainer.removeAllChildren();
			for each (var name:String in buttons)
			{			
				var button:Button = new Button();
				button.addEventListener(FlexEvent.CREATION_COMPLETE, buttonFocus);
				button.addEventListener(MouseEvent.CLICK, handleButtonClick);
				button.addEventListener(KeyboardEvent.KEY_DOWN, handleEnter);
				button.label = name;
				ButtonContainer.addChild(button);
			}
		}
		
		/**
		 * Adds a popup to the stage with the specified text message and list of
		 * buttons as options.  By default there is one button that says "OK"
		 * 
		 * @param text Message to display
		 * @param buttons String array of text that should appear on buttons.
		 * 					Default: "OK"
		 * @param buttonClick Optional callback to be called when a button is clicked
		 */
		public static function show(text:String, buttons:Array=null, buttonClick:Function=null):void
		{
			if (messageInstance[text] == null)
			{
				messageInstance[text] = new CustomMessage();
				messageInstance[text].text = text;
				messageInstance[text].buttonClick = buttonClick;
				if (buttons != null)
					messageInstance[text].buttons = buttons;
				
				PopUpManager.addPopUp(messageInstance[text], UIComponent(Application.application), true);
				PopUpManager.centerPopUp(messageInstance[text]);
			}
		}
		
		/**
		 * Resize the popup according to the amount of text that is being
		 * displayed.  If the text is larger than the screen size the popup
		 * on to the screen and let text scroll.  Also enforce a minimum height.
		 */
		public function resize():void
		{
			// Initial size settings
			var windowPadding:Number = 50;
			var messagePadding:Number = 20;
			var buttonMargin:Number = 110;
			var idealPopupWidth:Number = minWindowWidth;
			
			// Use a text field to measure text height
			var tf:TextField = new TextField();
			tf.multiline = false;
			tf.wordWrap = false;
			tf.htmlText = TextContainer.htmlText;
			
			// Make sure width is within min and max
			var idealMessageWidth:Number = tf.textWidth + messagePadding;
			idealPopupWidth = idealMessageWidth + messagePadding;
			if (idealPopupWidth > maxWindowWidth)
			{
				idealPopupWidth = maxWindowWidth;
				idealMessageWidth = idealPopupWidth - messagePadding;
			}
			else if (idealPopupWidth < minWindowWidth)
			{
				idealPopupWidth = minWindowWidth;
				idealMessageWidth = idealPopupWidth - messagePadding;
			}
			
			// Set window and message container width
			this.width = idealPopupWidth;
			tf.width = idealMessageWidth;
			TextContainer.width = idealMessageWidth;
			
			// Wrap lines to new width
			tf.multiline = true;
			tf.wordWrap = true;
			tf.htmlText = TextContainer.htmlText;
			
			var idealPopupHeight:Number = tf.textHeight + buttonMargin;
			
			// Check if popup is larger than the app
			if (idealPopupHeight > Application.application.height)
			{
				idealPopupHeight = Application.application.height - windowPadding;
				
				// Enforce a minimum height
				if (idealPopupHeight < minWindowHeight)
					idealPopupHeight = minWindowHeight;
			}
			
			this.height = idealPopupHeight;
		}
		
		/**
		 * Callback for any button click which closes the popup
		 */
		private function handleButtonClick(e:Event):void
		{
			if (messageInstance[text].buttonClick != null)
			{
				var event:CustomMessageEvent = new CustomMessageEvent();
				event.ButtonText = e.target.label;
				event.Message = text;
				messageInstance[text].buttonClick(event);
			}
			PopUpManager.removePopUp(messageInstance[text]);
			messageInstance[text] = null;
		}
		
		/**
		 * Handles keyboard event on button that has focus and calls the
		 * button click event if Enter is pressed
		 */
		private function handleEnter(e:KeyboardEvent):void
		{
			if (e.keyCode == Keyboard.ENTER)
				handleButtonClick(e);
		}
		
		/**
		 * Sets up button focus when buttons in the repeater are created
		 */
		private function buttonFocus(e:Event):void
		{
			var b:Button = e.currentTarget as Button;
			b.setFocus();
			focusManager.showFocus();
		}
	}
}