package com.dbi.cat.view.workspace
{
	import com.dbi.cat.common.vo.ConnectorVO;
	import com.dbi.cat.common.vo.LayoutInfoVO;
	import com.dbi.cat.event.CampaignEvent;
	import com.dbi.cat.event.WorkspaceEvent;
	
	import flash.display.DisplayObject;
	import flash.display.Graphics;
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.geom.Point;
	
	import mx.binding.utils.BindingUtils;
	import mx.binding.utils.ChangeWatcher;
	import mx.core.UIComponent;
	import mx.events.FlexEvent;
	import mx.utils.ObjectUtil;

	public class Connection extends WorkspaceItem implements ILayoutInfoItem
	{
		public static const MODE_CONNECT_ENDS:String = "connectEnds";
		public static const MODE_CONNECT_SOURCE:String = "connectSource";
		public static const MODE_CONNECT_DESTINATION:String = "connectDestination";
		
		public static const CONNECTION_BUFFER:Number = 2;
		public static const ARROW_LENGTH:Number = 20;
		public static const ARROW_WIDTH:Number = 5;
		
		
		public function get layoutInfo():LayoutInfoVO
		{
			return connectorVO.layoutInfo;
		}
		public function set layoutInfo(info:LayoutInfoVO):void
		{
			connectorVO.layoutInfo = info;
		}
			
		public function get voUID():String
		{
			return connectorVO.uid;
		}
			
		private var _mode:String = MODE_CONNECT_ENDS;
		public function get mode():String
		{
			return _mode;
		}
		public function set mode(m:String):void
		{
			_mode = m;
			
			if (mode == MODE_CONNECT_ENDS &&
				workspace != null)
			{
				workspace.removeEventListener(MouseEvent.MOUSE_MOVE, onMoveEndPoint);
			}
		}
		
		// Arrow component sprites
		public var connectToLine:UIComponent;
		public var connectFromLine:UIComponent;
		public var connectToArrow:UIComponent;
		public var connectFromArrow:UIComponent;
		
        [Embed("/assets/swf/play.swf")]
        public static const connectSourceIcon:Class;
        
        [Embed("/assets/swf/stop.swf")]
        public static const connectDestinationIcon:Class;
		
		// Components to draw connection between
		private var _fromNode:Node;
		public function get fromNode():Node
		{
			return _fromNode;
		}
		public function set fromNode(node:Node):void
		{
			_fromNode = node;
			ChangeWatcher.watch(fromNode, "x", function(e:Event):void{invalidateDisplayList()});
			ChangeWatcher.watch(fromNode, "y", function(e:Event):void{invalidateDisplayList()});
			redrawLine();
		}
		
		private var _toNode:Node;
		public function get toNode():Node
		{
			return _toNode;
		}
		public function set toNode(node:Node):void
		{
			_toNode = node;
			ChangeWatcher.watch(toNode, "x", function(e:Event):void{invalidateDisplayList()});
			ChangeWatcher.watch(toNode, "y", function(e:Event):void{invalidateDisplayList()});
			redrawLine();
		}
		
		// Cast to/from components to DisplayObjects
		public function get fromDisplay():DisplayObject
		{
			return fromNode as DisplayObject
		}
		public function get toDisplay():DisplayObject
		{
			return toNode as DisplayObject
		}
			
		// VO object
		private var _connectorVO:ConnectorVO;
		[Bindable]
		public function get connectorVO():ConnectorVO
		{
			return _connectorVO;
		}
		public function set connectorVO(value:ConnectorVO):void
		{
			_connectorVO = value;
			editConnectorVO = ObjectUtil.copy(connectorVO) as ConnectorVO;
			BindingUtils.bindProperty(this, "title", editConnectorVO, "name");
			dispatchEvent(new Event("checkValidState"));
			dispatchEvent(new Event("connectorUpdate"));
		}
		[Bindable]
		public var editConnectorVO:ConnectorVO;
		
		// Validation properties
		[Bindable(event="checkValidState")]
		public function get valid():Boolean
		{
			return true;
		}
		[Bindable(event="checkValidState")]
		public function get inValid():Boolean
		{
			return !valid;
		}
		
		public function Connection()
		{
			super();
			
			setStyle("backgroundImage", null);
			
			connectToLine = new UIComponent();
			connectFromLine = new UIComponent();
			connectToArrow = new UIComponent();
			connectFromArrow = new UIComponent();
			
			
			connectFromLine.addEventListener(MouseEvent.MOUSE_DOWN, onFromMouseDown);
			connectFromArrow.addEventListener(MouseEvent.MOUSE_DOWN, onFromMouseDown);
			connectToArrow.addEventListener(MouseEvent.MOUSE_DOWN, onToMouseDown);
			connectToLine.addEventListener(MouseEvent.MOUSE_DOWN, onToMouseDown);
			
			addEventListener(WorkspaceEvent.CLOSE_EDIT_MENU, onCloseMenu);
			addEventListener(FlexEvent.CREATION_COMPLETE, function(e:FlexEvent):void{invalidateDisplayList()});
			
			// Redraw connection lines when position changes
			ChangeWatcher.watch(this, "x", function(e:Event):void{invalidateDisplayList()});
			ChangeWatcher.watch(this, "x", function(e:Event):void{invalidateDisplayList()});
			
			// Bind warning icon to inValid property
			BindingUtils.bindProperty(invalidWarningLoader, "visible", this, "inValid");
		}
		
		override protected function updateDisplayList(w:Number, h:Number):void
		{
			super.updateDisplayList(w, h);
			redrawLine();
		}
		override protected function childrenCreated():void
		{
			super.childrenCreated();
			
			// Make sure connection lines are below everything
			addChildAt(connectToArrow, 0);
			addChildAt(connectFromArrow, 0);
			addChildAt(connectToLine, 0);
			addChildAt(connectFromLine, 0);
		}
		private function onCloseMenu(e:WorkspaceEvent):void
		{
			// Save this VO whenever the menu is closed
			var event:CampaignEvent = new CampaignEvent(CampaignEvent.SAVE_CONNECTOR);
			event.connector = editConnectorVO;
			dispatchEvent(event);
			
			// Update validation
			dispatchEvent(new Event("checkValidState"));
		}
		private function onFromMouseDown(e:MouseEvent):void
		{
			e.stopPropagation();
			dispatchEvent(new WorkspaceEvent(WorkspaceEvent.CHANGE_CONNECTION_SOURCE));
			if (workspace != null)
				workspace.addEventListener(MouseEvent.MOUSE_MOVE, onMoveEndPoint);
		}
		private function onToMouseDown(e:MouseEvent):void
		{
			e.stopPropagation();
			dispatchEvent(new WorkspaceEvent(WorkspaceEvent.CHANGE_CONNECTION_DESTINATION));
			if (workspace != null)
				workspace.addEventListener(MouseEvent.MOUSE_MOVE, onMoveEndPoint);
		}
		private function onMoveEndPoint(e:MouseEvent):void
		{
			invalidateDisplayList();
		}

		public function redrawLine():void
		{
			if (parent != null)
			{
				var mousePoint:Point = new Point(parent.mouseX - x, parent.mouseY - y);
					
				// Get center of icon
				var centerX:Number = width/2;
				var centerY:Number = height/2;
				
				// Draw line to mouse if connecting destination
				if (mode == MODE_CONNECT_DESTINATION)
				{
					var xTo:Number = mousePoint.x - CONNECTION_BUFFER;
					var yTo:Number = mousePoint.y + CONNECTION_BUFFER;
				}
				// If connected to a target draw line to it
				else if (toDisplay != null)
				{
					// Translate between coordinate systems
					var global:Point = toDisplay.parent.localToGlobal(new Point(toDisplay.x, toDisplay.y));
					var local:Point = globalToLocal(new Point(global.x, global.y));
					xTo = local.x + toDisplay.width/2;
					yTo = local.y + toDisplay.height/2;
				}
				// If no target draw line straight down
				else
				{
					xTo = width/2;
					yTo = height*2;
				}
				
				// If mode is connect source draw line from mouse
				if (mode == MODE_CONNECT_SOURCE)
				{
					var xFrom:Number = mousePoint.x - CONNECTION_BUFFER;
					var yFrom:Number = mousePoint.y + CONNECTION_BUFFER;
				}
				// If connected to a source draw line from
				else if (fromDisplay != null)
				{
					// Translate between coordinate systems
					global = fromDisplay.parent.localToGlobal(new Point(fromDisplay.x, fromDisplay.y));
					local = globalToLocal(new Point(global.x, global.y));
					xFrom = local.x + fromDisplay.width/2;
					yFrom = local.y + fromDisplay.height/2;
					
				}
				// If no destination draw line straight up
				else
				{
					xFrom = width/2;
					yFrom = -height;
				}
				
				// Get line angles
				var toAngle:Number = Math.atan2(centerY - yTo, centerX - xTo);
				var fromAngle:Number = Math.atan2(yFrom - centerY, xFrom - centerX);
				
				
				// Draw From line
				var g:Graphics = connectFromLine.graphics;
				g.clear();
				g.lineStyle(1, 0x000000);
				g.moveTo(xFrom, yFrom);
				g.lineTo(centerX, centerY);
				g.endFill();
				
				// Draw To line
				g = connectToLine.graphics;
				g.clear();
				g.lineStyle(1, 0x000000);
				g.moveTo(centerX, centerY);
				g.lineTo(xTo, yTo);
				g.endFill()
				
				// Draw To Arrow
				if (toDisplay != null)
				{
					var tipX:Number = centerX + (xTo - centerX)/2;
					var tipY:Number = centerY + (yTo - centerY)/2;
				}
				else
				{
					tipX = xTo - Math.cos(toAngle) * 2;
					tipY = yTo - Math.sin(toAngle) * 2;
				}
				var originalX:Number = ARROW_LENGTH;
				var originalY:Number = -ARROW_WIDTH;
				
				g = connectToArrow.graphics;
				g.clear();
				g.beginFill(0x000000, 1);
				g.moveTo(tipX, tipY);
				g.lineTo(originalX * Math.cos(toAngle) - originalY * Math.sin(toAngle) + tipX,
						 originalY * Math.cos(toAngle) + originalX * Math.sin(toAngle) + tipY);
						 
				originalY = ARROW_WIDTH;
				g.lineTo(originalX * Math.cos(toAngle) - originalY * Math.sin(toAngle) + tipX,
						 originalY * Math.cos(toAngle) + originalX * Math.sin(toAngle) + tipY);
				g.lineTo(tipX, tipY);
				g.endFill();
				
				// Draw From Arrow
				if (fromDisplay != null)
				{
					tipX = xFrom + (centerX - xFrom)/2;
					tipY = yFrom + (centerY - yFrom)/2;
				}
				else
				{
					tipX = xFrom - Math.cos(fromAngle) * 2;
					tipY = yFrom - Math.sin(fromAngle) * 2;
				}
				originalX = ARROW_LENGTH;
				originalY = -ARROW_WIDTH;
				
				g = connectFromArrow.graphics;
				g.clear();
				g.beginFill(0x000000, 1);
				g.moveTo(tipX, tipY);
				g.lineTo(originalX * Math.cos(fromAngle) - originalY * Math.sin(fromAngle) + tipX,
						 originalY * Math.cos(fromAngle) + originalX * Math.sin(fromAngle) + tipY);
						 
				originalY = ARROW_WIDTH;
				g.lineTo(originalX * Math.cos(fromAngle) - originalY * Math.sin(fromAngle) + tipX,
						 originalY * Math.cos(fromAngle) + originalX * Math.sin(fromAngle) + tipY);
				g.lineTo(tipX, tipY);
				g.endFill();
			}
		}
	}
}