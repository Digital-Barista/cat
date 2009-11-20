package com.dbi.cat.view.workspace
{
	import mx.binding.utils.BindingUtils;
	import mx.containers.Canvas;
	import mx.controls.SWFLoader;
	import mx.controls.TextArea;

	public class IconItem extends Canvas
	{
		// Icons
        [Embed("/assets/swf/plus.swf")]
        public static const editIcon:Class;
    
        [Embed("/assets/swf/delete.swf")]
        public static const deleteIcon:Class;
        
        [Embed("/assets/swf/house.swf")]
        public static const entryPointIcon:Class;
        
        [Embed("/assets/swf/envelope.swf")]
        public static const messageIcon:Class;
        
        [Embed("/assets/swf/barcode.swf")]
        public static const couponIcon:Class;
        
        [Embed("/assets/swf/date_connector.swf")]
        public static const dateConnector:Class;
        
        [Embed("/assets/swf/date_connector_workspace.swf")]
        public static const dateConnectorWorkspace:Class;
        
        [Embed("/assets/swf/interval_connector.swf")]
        public static const intervalConnector:Class;
        
        [Embed("/assets/swf/interval_connector_workspace.swf")]
        public static const intervalConnectorWorkspace:Class;
        
        [Embed("/assets/swf/immediate_connector.swf")]
        public static const immediateConnector:Class; 
        
        [Embed("/assets/swf/immediate_connector_workspace.swf")]
        public static const immediateConnectorWorkspace:Class; 
        
        [Embed("/assets/swf/response_connector.swf")]
        public static const responseConnector:Class;
        
        [Embed("/assets/swf/response_connector_workspace.swf")]
        public static const responseConnectorWorkspace:Class;
        
        [Embed("/assets/swf/green_circle.swf")]
        public static const backgroundIcon:Class;
        
        [Embed("/assets/swf/exclamation_point.swf")]
        public static const exclamationPointIcon:Class;
        
        // UIComponents
		private var _displayLoader:SWFLoader;
		public function get displayLoader():SWFLoader
		{
			return _displayLoader;
		}
		public function set displayLoader(loader:SWFLoader):void
		{
			_displayLoader = loader;
		}
		protected var nameLabel:TextArea;
		
		// Display properties
		private var _displayIcon:Object;
		public function get displayIcon():Object
		{
			return _displayIcon;
		}
		public function set displayIcon(icon:Object):void
		{
			_displayIcon = icon;
			displayLoader.source = icon;
		}
		
		public var workspaceIcon:Object;
		private var _title:String;
		[Bindable]
		public function get title():String
		{
			return _title;
		}
		public function set title(value:String):void
		{
			_title = value;
		}
		
		public function IconItem()
		{
			super();
			
			// Set canvas properties
			horizontalScrollPolicy = "off";
			verticalScrollPolicy = "off";
			clipContent = false;
			
			// Create display components
			displayLoader = new SWFLoader();
			nameLabel = new TextArea();
			
			// Position elements
			displayLoader.setStyle("horizontalAlign", "center");
			displayLoader.setStyle("verticalAlign", "middle");
			displayLoader.percentHeight = 100;
			displayLoader.percentWidth = 100;
			
			nameLabel.wordWrap = true;
			nameLabel.editable = false;
			nameLabel.maxWidth = 100;
			nameLabel.maxHeight = 20;
			nameLabel.width = 100;
			nameLabel.height = 20;
			nameLabel.selectable = false;
			nameLabel.setStyle("textAlign", "center");
			nameLabel.setStyle("backgroundAlpha", 0);
			nameLabel.setStyle("borderStyle", "none");
			nameLabel.setStyle("bottom", -20);
			nameLabel.setStyle("horizontalCenter", 0);
			
			BindingUtils.bindProperty(nameLabel, "text", this, "title");
		}
		protected override function createChildren():void
		{
			super.createChildren();
			
			// Add children to canvas
			addChild(displayLoader);
			addChild(nameLabel);
		}
	}
}