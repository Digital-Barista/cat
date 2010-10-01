package com.dbi.cat.view.workspace
{
	import com.dbi.cat.common.vo.LayoutInfoVO;
	import com.dbi.controls.CustomMessage;
	import com.dbi.event.CustomMessageEvent;
	
	import flash.events.MouseEvent;
	import flash.filters.GlowFilter;
	import flash.geom.Point;
	
	import mx.containers.TitleWindow;
	import mx.controls.SWFLoader;
	import mx.core.UIComponent;
	import mx.core.UITextField;
	import mx.effects.Fade;
	import mx.effects.Move;
	import mx.effects.Parallel;
	import mx.effects.Resize;
	import mx.events.CloseEvent;
	import mx.events.EffectEvent;
	import mx.managers.PopUpManager;

	[Event(name="selectWorkspaceItem", type="com.dbi.cat.event.WorkspaceEvent")]
	[Event(name="unselectWorkspaceItem", type="com.dbi.cat.event.WorkspaceEvent")]
	[Event(name="unselectAllWorkspaceItems", type="com.dbi.cat.event.WorkspaceEvent")]
	[Event(name="connectItem", type="com.dbi.cat.event.WorkspaceEvent")]
	[Event(name="removeWorkspaceItem", type="com.dbi.cat.event.WorkspaceEvent")]
	[Event(name="bringToFront", type="com.dbi.cat.event.WorkspaceEvent")]
	[Event(name="changeConnectionSource", type="com.dbi.cat.event.WorkspaceEvent")]
	[Event(name="changeConnectionDestination", type="com.dbi.cat.event.WorkspaceEvent")]
	[Event(name="openEditWindow", type="com.dbi.cat.event.WorkspaceEvent")]
	[Event(name="closeEditWindow", type="com.dbi.cat.event.WorkspaceEvent")]
	
	[Bindable]
	public class WorkspaceItem extends IconItem
	{
		import com.dbi.cat.event.WorkspaceEvent;
		import mx.controls.Alert;
		import mx.collections.ArrayCollection;
		import mx.events.MenuEvent;
		import mx.controls.Menu;
		
		public static const MENU_ICON_SIZE:Number = 25;
		public static const WORK_SPACE_ITEM_WIDTH:Number = 75;
		
		protected var dragging:Boolean = false;
		
		/**
		 * Determines if anything in this item can be edited
		 */
		 private var _readonly:Boolean = false;
		 public function get readonly():Boolean
		 {
		 	return _readonly;
		 }
		 public function set readonly(value:Boolean):void
		 {
		 	_readonly = value;
		 	setEditState();
		 	
			// Change event listeners and delete menu
			if (readonly)
			{
				if (getChildren().indexOf(deleteLoader) > -1)
					removeChild(deleteLoader);
			}
			else
			{
				if (getChildren().indexOf(deleteLoader) < 0)
					addChild(deleteLoader);
			}
		 }
		 
		/**
		 * Flag to determine if invalid icon should show
		 */
		 public var showInvalidWarning:Boolean = true;
		 
		// Selected properties
		private var selectGlow:GlowFilter = new GlowFilter(0x0000DD, 0.5, 10, 10);
		
		private var _selected:Boolean = false;
		public function get selected():Boolean
		{
			return _selected;
		}
		public function set selected(value:Boolean):void
		{
			_selected = value
			if (selected)
				filters = [selectGlow];
			else
				filters = [];
		}
		
		
		// Edit window sizes
		public var editWindowWidth:Number = 300;
		public var editWindowHeight:Number = 150;
		
		
		// Workspace this workspace item is on
		public function get workspace():WorkspaceView
		{
			if (parent != null  &&
				parent.parent != null &&
				parent.parent.parent != null &&
				parent.parent.parent is WorkspaceView)
				return parent.parent.parent as WorkspaceView;
			return null;
		}
		
		// Layout properties
		protected var deleteLoader:SWFLoader;
		protected var editLoader:SWFLoader;
		protected var invalidWarningLoader:SWFLoader;
		public var editWindow:TitleWindow = new TitleWindow();
		
		// Effects
		private var editWindowEffect:Parallel;
		
		// Edit window content
		public function set editContent(value:Array):void
		{
			for each (var display:UIComponent in value)
			{
				editWindow.addChild(display);
			}
			setEditState();
		}
		
		// Constructor
		public function WorkspaceItem()
		{
			super();
				
			// Setup icon for opening edit menu
			editLoader = new SWFLoader();
			editLoader.source = editIcon;
			
			editLoader.width = MENU_ICON_SIZE;
			editLoader.height = MENU_ICON_SIZE;
			editLoader.setStyle("right", 0);
			editLoader.setStyle("top", 0);
			editLoader.addEventListener(MouseEvent.CLICK, editClick, false, 0, true);
			editLoader.toolTip = "Edit this items properties";
			editLoader.buttonMode = true;
			
			// Setup warning loader
			invalidWarningLoader = new SWFLoader();
			invalidWarningLoader.width = MENU_ICON_SIZE;
			invalidWarningLoader.height = MENU_ICON_SIZE;
			invalidWarningLoader.setStyle("right", 0);
			invalidWarningLoader.setStyle("bottom", 0);
			invalidWarningLoader.source = exclamationPointIcon;
			invalidWarningLoader.toolTip = "This item is invalid and cannot be published";
			invalidWarningLoader.addEventListener(MouseEvent.CLICK, editClick, false, 0, true);
			
			// Setup window that will hold edit controls
//			editWindow.x = width/2;
//			editWindow.y = height/2;
//			editWindow.alpha = 0;
			editWindow.showCloseButton = true;
			editWindow.verticalScrollPolicy = "off";
			editWindow.horizontalScrollPolicy = "off";
			editWindow.addEventListener(CloseEvent.CLOSE, onCloseEdit, false, 0, true);
			editWindow.addEventListener(MouseEvent.MOUSE_DOWN, function(e:MouseEvent):void{e.stopPropagation()});
			
			// Setup delete menu
			deleteLoader = new SWFLoader();
			deleteLoader.source = deleteIcon;
			deleteLoader.width = MENU_ICON_SIZE;
			deleteLoader.height = MENU_ICON_SIZE;
			deleteLoader.addEventListener(MouseEvent.CLICK, deleteClick, false, 0, true);
			deleteLoader.toolTip = "Remove this item";
			deleteLoader.buttonMode = true;
			
			// Add event handlers
			addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
			addEventListener(MouseEvent.MOUSE_UP, onMouseUp);
		}
		
		private function setEditState():void
		{
			for each (var child:UIComponent in editWindow.getChildren())
			{
				child.enabled = !readonly;
			}
		}
		protected override function createChildren():void
		{
			super.createChildren();
			
			// Add children to canvas
			addChild(editLoader);
			
			if (showInvalidWarning)
				addChild(invalidWarningLoader);
			
			if (!readonly)
				addChild(deleteLoader);
			
			
			// Set title of edit window
			editWindow.title = label;
		}
		
		/**
		 * Update the x, y position of this item when the layout
		 * info is changed
		 * 
		 * @param info LayoutInfoVO containing x, y positions
		 */
		protected function onLayoutInfoChanged(info:LayoutInfoVO):void
		{
			if (info != null)
			{
				x = info.x;
				y = info.y;
			}
		}
		
		private function onMouseDown(e:MouseEvent):void
		{
			// Don't do any actions if the user has clicked on the title label
			if (!readonly &&
				!(e.target is UITextField))
			{
				// Unselect all items if shift isn't selected and the current item isn't selected
				if (!e.ctrlKey &&
					!selected)
					dispatchEvent(new WorkspaceEvent(WorkspaceEvent.UNSELECT_ALL_WORKSPACE_ITEMS));
					
				// Deselect if selected and holding control key
				if (e.ctrlKey &&
					selected)
				{
					var event:WorkspaceEvent = new WorkspaceEvent(WorkspaceEvent.UNSELECT_WORKSPACE_ITEM);
					event.workspaceItem = this;
					dispatchEvent(event);
				}
				// Select this item
				else
				{
					event = new WorkspaceEvent(WorkspaceEvent.SELECT_WORKSPACE_ITEM);
					event.workspaceItem = this;
					dispatchEvent(event);
				}
				
				// Bring clicked nodes to the front of the stage
				if (this is Node)
				{
		            event = new WorkspaceEvent(WorkspaceEvent.BRING_TO_FRONT);
		            event.workspaceItem = this;
		            dispatchEvent(event);
		  		}
		 	}
		}
		private function onMouseUp(e:MouseEvent):void
		{
			if (!readonly)
				dispatchEvent(new WorkspaceEvent(WorkspaceEvent.CONNECT_ITEM));
		}
		private function editClick(e:MouseEvent):void
		{
			dispatchEvent(new WorkspaceEvent(WorkspaceEvent.OPEN_EDIT_MENU));
		}
		private function onCloseEdit(e:CloseEvent):void
		{
			closeEditWindow();
		}
		private function deleteClick(e:MouseEvent):void
		{
			CustomMessage.show("Are you sure you want to delete this item?",
				["Yes", "No"],
				confirmDelete);
		}
		private function confirmDelete(e:CustomMessageEvent):void
		{
			if (e.ButtonText == "Yes")
			{
	            var event:WorkspaceEvent = new WorkspaceEvent(WorkspaceEvent.REMOVE_WORKSPACE_ITEM);
	            event.workspaceItem = this;
	            dispatchEvent(event);
	  		}
		}
		
		public function openEditWindow():void
		{
			if (workspace != null)
			{
				editWindow.width = editWindowWidth;
				editWindow.height = editWindowHeight;
				
				PopUpManager.addPopUp(editWindow, workspace, true);
				PopUpManager.centerPopUp(editWindow);
			}
		}
		public function closeEditWindow():void
		{
			if (workspace != null)
			{
				// Save item if not readonly
				if (!readonly)
					dispatchEvent(new WorkspaceEvent(WorkspaceEvent.CLOSE_EDIT_MENU));
				
				PopUpManager.removePopUp(editWindow);
			}
		}
		private function closeEditEffectEnd(e:EffectEvent):void
		{
			if (workspace != null &&
				workspace.getChildren().indexOf(editWindow) > -1)
				workspace.removeChild(editWindow);
			
		}
		
		private function itemClick():void
		{
			dispatchEvent(new WorkspaceEvent(WorkspaceEvent.BRING_TO_FRONT));
		}
	}
}