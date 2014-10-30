package com.dbi.cat.event
{
	import com.dbi.cat.view.components.SelectionBox;
	import com.dbi.cat.view.workspace.WorkspaceItem;
	import com.dbi.cat.view.workspace.WorkspaceView;
	
	import flash.events.Event;

	public class WorkspaceEvent extends Event
	{
		public static const CHANGE_CONNECTION_SOURCE:String = "changeConnectionSource";
		public static const CHANGE_CONNECTION_DESTINATION:String = "changeConnectionDestination";
		public static const BRING_FORWARD:String = "bringForward";
		public static const BRING_TO_FRONT:String = "bringToFront";
		public static const OPEN_EDIT_MENU:String = "openEditMenu";
		public static const CLOSE_EDIT_MENU:String = "closeEditMenu";
		public static const CONNECT_ITEM:String = "connectItem";
		public static const REMOVE_WORKSPACE_ITEM:String = "removeWorkspaceItem";
		public static const REMOVE_SELECTED_WORKSPACE_ITEMS:String = "removeSelectedWorkspaceItems";
		public static const SELECT_WORKSPACE_ITEM:String = "selectWorkspaceItem";
		public static const UNSELECT_WORKSPACE_ITEM:String = "unSelectWorkspaceItem";
		public static const UNSELECT_ALL_WORKSPACE_ITEMS:String = "unSelectAllWorkspaceItems";
		public static const SELECTION_BOX_SELECT:String = "selectionBoxSelect";
		public static const FIT_CONTENT:String = "fitContent";
		public static const LOAD_CAMPAIGN:String = "loadCampaign";
		public static const ZOOM_IN:String = "zoomInEvent";
		public static const ZOOM_OUT:String = "zoomOutEvent";
		public static const ALIGN_LEFT:String = "alignLeftEvent";
		public static const ALIGN_RIGHT:String = "alignRightEvent";
		public static const ALIGN_BOTTOM:String = "alignBottomEvent";
		public static const ALIGN_TOP:String = "alignTopEvent";
		
		public var workspaceItem:WorkspaceItem;
		
		public function WorkspaceEvent(type:String)
		{
			super(type, true, false);
		}
		
		override public function clone():Event
		{
			return new WorkspaceEvent(type);
		}
	}
}