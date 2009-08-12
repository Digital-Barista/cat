package com.dbi.cat.view.workspace
{
	import com.dbi.cat.constants.WorkspaceItemType;

	[Bindable]
	public class CommunicationComponent extends IconItem
	{
		import com.dbi.cat.event.WorkspaceEvent;
		import mx.controls.Alert;
		import mx.collections.ArrayCollection;
		import mx.events.MenuEvent;
		import mx.controls.Menu;
		
		// Icon properties based on type
		public function get componentIcon():Object
		{
			var ret:Object = null;
			switch (type)
			{
				case WorkspaceItemType.DATE_CONNECTOR:
					ret = dateConnector;
					break;
				case WorkspaceItemType.ENTRY_POINT:
					ret = entryPointIcon;
					break;
				case WorkspaceItemType.IMMEDIATE_CONNECTOR:
					ret = immediateConnector;
					break;	
				case WorkspaceItemType.INTERVAL_CONNECTOR:
					ret = intervalConnector;
					break;	
				case WorkspaceItemType.MESSAGE:
					ret = messageIcon;
					break;	
				case WorkspaceItemType.RESPONSE_CONNECTOR:
					ret = responseConnector;
					break;	
			}
			return ret;
		}
		
		private var _type:String;
		public function get type():String
		{
			return _type;
		}
		public function set type(value:String):void
		{
			_type = value;
			title = value;
			displayLoader.source = componentIcon;
		}
		
		// Constructor
		public function CommunicationComponent()
		{
			super();
		}
		
	}
}