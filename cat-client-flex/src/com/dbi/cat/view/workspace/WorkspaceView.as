package com.dbi.cat.view.workspace
{
	import com.dbi.cat.common.vo.CalendarConnectorVO;
	import com.dbi.cat.common.vo.CampaignVO;
	import com.dbi.cat.common.vo.ConnectorVO;
	import com.dbi.cat.common.vo.CouponVO;
	import com.dbi.cat.common.vo.EntryPointDefinitionVO;
	import com.dbi.cat.common.vo.EntryPointVO;
	import com.dbi.cat.common.vo.ImmediateConnectorVO;
	import com.dbi.cat.common.vo.IntervalConnectorVO;
	import com.dbi.cat.common.vo.LayoutInfoVO;
	import com.dbi.cat.common.vo.MessageVO;
	import com.dbi.cat.common.vo.NodeVO;
	import com.dbi.cat.common.vo.ResponseConnectorVO;
	import com.dbi.cat.constants.WorkspaceItemType;
	import com.dbi.cat.event.CampaignEvent;
	import com.dbi.cat.event.LayoutInfoEvent;
	import com.dbi.cat.event.WorkspaceEvent;
	import com.dbi.cat.view.components.PanZoomCanvas;
	import com.dbi.cat.view.components.SelectionBox;
	
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.geom.Point;
	
	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.collections.Sort;
	import mx.collections.SortField;
	import mx.containers.Canvas;
	import mx.core.Application;
	import mx.events.DragEvent;
	import mx.managers.DragManager;

			
	public class WorkspaceView extends Canvas
	{
		public static const WORKSPACE_ITEM_WIDTH:Number = 75;
		
		// Main view components
		public var zoomContainer:PanZoomCanvas;
		public var selectionBox:SelectionBox;
		public var workspaceControls:WorkspaceControls;
		
		// Map of changewatchers
		private var changeWatcherMap:Object = new Object();
		
		private var _readonly:Boolean = false;
		[Bindable(event="updateReadonly")]
		public function get readonly():Boolean
		{
			return _readonly;
		}
		public function set readonly(value:Boolean):void
		{
			_readonly = value;
			dispatchEvent(new Event("updateReadonly"));
		}
		
		private var _campaign:CampaignVO;
		[Bindable]
		public function get campaign():CampaignVO
		{
			return _campaign;
		}
		public function set campaign(c:CampaignVO):void
		{
			if (campaign == null ||
				c == null ||
				campaign.uid != c.uid ||
				campaign.currentVersion != c.currentVersion )
			{
				_campaign = c;
				loadCampaign(c);
			}	
			_campaign = c;
			buildKeywordList();
		}
	
		// Workspace item properties
		private var connectToComponent:DisplayObject;
		public var openWorkspaceItem:WorkspaceItem;
		public var sourceConnection:Connection;
		public var destinationConnection:Connection;
		public var selectedWorkspaceItems:ArrayCollection = new ArrayCollection();
		
		// Mouse drag coordinates
		private var lastMouseX:Number;
		private var lastMouseY:Number;
		
		// Valid entrypoint lists
		private var _clientMap:Object;
		public function get clientMap():Object
		{
			return _clientMap;
		}
		public function set clientMap(value:Object):void
		{
			_clientMap = value;
			buildKeywordList();
		}
		
		[Bindable]
		public var campaignKeywords:ArrayCollection
		
		public function WorkspaceView()
		{
			super();
			
			// Setup properties
			doubleClickEnabled = true;
			horizontalScrollPolicy = "off";
			verticalScrollPolicy = "off";
			
			// Add main components
			zoomContainer = new PanZoomCanvas();
			zoomContainer.setStyle("left", 0);
			zoomContainer.setStyle("right", 0);
			zoomContainer.setStyle("top", 0);
			zoomContainer.setStyle("bottom", 0);
			addChild(zoomContainer);
			
			selectionBox = new SelectionBox();
			addChild(selectionBox);
			
			workspaceControls = new WorkspaceControls();
			workspaceControls.percentWidth = 100;
			workspaceControls.height = 25;
			workspaceControls.addEventListener(WorkspaceEvent.FIT_CONTENT, function(e:WorkspaceEvent):void{fitContent()});
			workspaceControls.addEventListener(WorkspaceEvent.ZOOM_IN, function(e:WorkspaceEvent):void{zoomContainer.doZoom(true, new Point(width/2, height/2))});
			workspaceControls.addEventListener(WorkspaceEvent.ZOOM_OUT, function(e:WorkspaceEvent):void{zoomContainer.doZoom(false, new Point(width/2, height/2))});
			workspaceControls.addEventListener(WorkspaceEvent.ALIGN_LEFT, alignLeft);
			workspaceControls.addEventListener(WorkspaceEvent.ALIGN_BOTTOM, alignBottom);
			workspaceControls.addEventListener(WorkspaceEvent.ALIGN_RIGHT, alignRight);
			workspaceControls.addEventListener(WorkspaceEvent.ALIGN_TOP, alignTop);
			workspaceControls.readonly = readonly;
			BindingUtils.bindProperty(workspaceControls, "readonly", this, "readonly");
			addChild(workspaceControls);	
			
    		addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
    		addEventListener(MouseEvent.MOUSE_UP, onMouseUp);
    		addEventListener(DragEvent.DRAG_ENTER, dragEnterHandler);
    		addEventListener(DragEvent.DRAG_DROP, dragDropHandler);
		}
		
		
		private function buildKeywordList():void
		{
			campaignKeywords = new ArrayCollection();
			
			if (campaign != null &&
				clientMap != null &&
				clientMap[campaign.clientPK] != null)
			{
				for each (var e:EntryPointDefinitionVO in clientMap[campaign.clientPK].entryPoints)
				{
					if (e.type == campaign.type &&
						e.value == campaign.defaultFromAddress)
					{
						campaignKeywords = e.keywords;
						break;
					}
				}
				
				// Sort keywords
				var sort:Sort = new Sort();
				sort.fields = [new SortField("keyword")];
				campaignKeywords.sort = sort;
				campaignKeywords.refresh();
			}
		}
		
		public function fitContent():void
		{
			zoomContainer.fitComponents();
		}
		
		// Called if the user drags a drag proxy onto the drop target.
        private function dragEnterHandler(event:DragEvent):void 
        {
        	if (!readonly)
        	{
	            // Get the drop target component from the event object.
	            var dropTarget:WorkspaceView = event.currentTarget as WorkspaceView;
	
	            // Accept the drag only if the user is dragging data 
	            // identified by the 'value' format value.
	            if (event.dragSource.hasFormat("value")) 
	            {
	                // Accept the drop.
	                DragManager.acceptDragDrop(dropTarget);
	            }
         	}
        }
        
        // Called if the target accepts the dragged object and the user 
        // releases the mouse button while over the drop target. 
        private function dragDropHandler(e:DragEvent):void 
        {
        	if (!readonly)
        	{
	            // Get the swf loader that was dropped
	            var item:CommunicationComponent = e.dragSource.dataForFormat("value") as CommunicationComponent;
	            
	            createWorkspaceItem(item.type);
	        }
        }
        
        private function onMouseClick(e:MouseEvent):void
        {
        	if (!readonly &&
        		(e.target is WorkspaceView ||
        		e.target is PanZoomCanvas))
        	{
				endConnection();
				unSelectAllWorkspaceItems();
        	}
        }
        private function onMouseDown(e:MouseEvent):void
        {
        	if (e.shiftKey &&
        		!readonly)
        		selectionBox.startSelection(mouseX, mouseY);
        	
			if (openWorkspaceItem != null &&
        		(e.target is WorkspaceView ||
        		e.target is PanZoomCanvas))
        	{
				openWorkspaceItem.closeEditWindow();
				openWorkspaceItem = null;
        	}
        }
        private function onMouseUp(e:MouseEvent):void
        {
        	if (!readonly)
        	{
	        	if (selectionBox.isSelecting)
					selectSelectionBoxItems();
				endConnection();
        	}
        }
		
		//
		// WorkspaceItem methods
		//
		public function createWorkspaceItem(type:String):void
		{
			// Fire a save node event for the new type
			var saveEvent:CampaignEvent;
            
			// Create layout data
			var layout:LayoutInfoVO  = new LayoutInfoVO();
			layout.x = zoomContainer.mouseX - (WORKSPACE_ITEM_WIDTH/2);
			layout.y = zoomContainer.mouseY - (WORKSPACE_ITEM_WIDTH/2);
			layout.campaignUUID = campaign.uid;
			
			// Create correct type of VO
            if (type == WorkspaceItemType.MESSAGE)
            {
            	var message:MessageVO = new MessageVO();
            	message.campaignUID = campaign.uid;
            	layout.UUID = message.uid;
            	message.layoutInfo = layout;
            	
            	saveEvent = new CampaignEvent(CampaignEvent.SAVE_NODE);
            	saveEvent.node = message;
            	addNodeToWorkspace(message);
            }
            else if(type == WorkspaceItemType.ENTRY_POINT)
            {
            	var entryPoint:EntryPointVO = new EntryPointVO();
            	entryPoint.campaignUID = campaign.uid;
            	entryPoint.entryType = campaign.type;
            	entryPoint.entryPoint = campaign.defaultFromAddress;
            	layout.UUID = entryPoint.uid;
            	entryPoint.layoutInfo = layout;
            	
            	saveEvent = new CampaignEvent(CampaignEvent.SAVE_NODE);
				saveEvent.node = entryPoint;
				addNodeToWorkspace(entryPoint);
            }
            else if(type == WorkspaceItemType.COUPON)
            {
            	var coupon:CouponVO = new CouponVO();
            	coupon.campaignUID = campaign.uid;
            	layout.UUID = coupon.uid;
            	coupon.layoutInfo = layout;
            	
            	saveEvent = new CampaignEvent(CampaignEvent.SAVE_NODE);
				saveEvent.node = coupon;
				addNodeToWorkspace(coupon);
            }
            else if(type == WorkspaceItemType.IMMEDIATE_CONNECTOR)
            {
            	var immediate:ImmediateConnectorVO = new ImmediateConnectorVO();
            	immediate.campaignUID = campaign.uid;
            	layout.UUID = immediate.uid;
            	immediate.layoutInfo = layout;
            	
            	saveEvent = new CampaignEvent(CampaignEvent.SAVE_CONNECTOR);
            	saveEvent.connector = immediate;
            	addConnectorToWorkspace(immediate);
            }
            else if (type == WorkspaceItemType.DATE_CONNECTOR)
            {
            	var date:CalendarConnectorVO = new CalendarConnectorVO();
            	date.campaignUID = campaign.uid;
            	layout.UUID = date.uid;
            	date.layoutInfo = layout;
            	
            	saveEvent = new CampaignEvent(CampaignEvent.SAVE_CONNECTOR);
            	saveEvent.connector = date;
            	addConnectorToWorkspace(date);
            }
            else if (type == WorkspaceItemType.INTERVAL_CONNECTOR)
            {
            	var interval:IntervalConnectorVO = new IntervalConnectorVO();
            	interval.campaignUID = campaign.uid;
            	layout.UUID = interval.uid;
            	interval.layoutInfo = layout;
            	
            	saveEvent = new CampaignEvent(CampaignEvent.SAVE_CONNECTOR);
            	saveEvent.connector = interval;
            	addConnectorToWorkspace(interval);
            }
            else if (type == WorkspaceItemType.RESPONSE_CONNECTOR)
            {
            	var response:ResponseConnectorVO = new ResponseConnectorVO();
            	response.campaignUID = campaign.uid;
            	response.entryPointType = campaign.type;
            	response.entryPoint = campaign.defaultFromAddress;
            	layout.UUID = response.uid;
            	response.layoutInfo = layout;
            	
            	saveEvent = new CampaignEvent(CampaignEvent.SAVE_CONNECTOR);
            	saveEvent.connector = response;
            	addConnectorToWorkspace(response);
            }
            else
            {
            	throw new Error("Invalid workspace type: " + type);
            }
			
			// Dispatch event to add the item on the server
			dispatchEvent(saveEvent);
			
			// Dispatch event to add layout info
			var event:LayoutInfoEvent = new LayoutInfoEvent(LayoutInfoEvent.SAVE_LAYOUT_INFO);
			event.layoutInfo = layout;
			dispatchEvent(event);
			
		}
		private function addNodeToWorkspace(node:NodeVO):void
		{
			// Create new WorkspaceItem based on type
            var newItem:Node;
            
			if (node is MessageVO)
            {
            	var m:MessageItem = new MessageItem();
            	m.messageVO = node as MessageVO;
            	newItem = m;
            }
            else if(node is EntryPointVO)
            {
            	var ep:EntryPointItem = new EntryPointItem();
            	ep.entryPointVO = node as EntryPointVO;
				newItem = ep;
            }
            else if(node is CouponVO)
            {
            	var c:CouponItem = new CouponItem();
            	c.couponVO = node as CouponVO;
				newItem = c;
            }
            
            // Hide some icons for template campaigns
            newItem.showInvalidWarning = !campaign.isTemplate;
            newItem.showStatistics = !campaign.isTemplate;
            
            addItemToWorkspace(newItem);
		}
		private function addConnectorToWorkspace(connector:ConnectorVO):void
		{
            var newItem:Connection;
            
			if(connector is ImmediateConnectorVO)
            {
            	var imConn:ImmediateConnection = new ImmediateConnection();
            	imConn.connectorVO = connector as ImmediateConnectorVO;
            	newItem = imConn;
            }
            else if (connector is CalendarConnectorVO)
            {
            	var dc:DateConnection = new DateConnection();
            	dc.connectorVO = connector as CalendarConnectorVO;
            	newItem = dc;
            }
            else if (connector is IntervalConnectorVO)
            {
            	var ic:IntervalConnection = new IntervalConnection();
            	ic.connectorVO = connector as IntervalConnectorVO;
            	newItem = ic;
            }
            else if (connector is ResponseConnectorVO)
            {
            	var rc:ResponseConnection = new ResponseConnection();
            	rc.connectorVO = connector as ResponseConnectorVO;
            	newItem = rc;
            }
            
            // Hide some icons for template campaigns
            newItem.showInvalidWarning = !campaign.isTemplate;
            
            // Associate connected nodes
            if (connector.sourceNodeUID != null)
            {
            	for each (var item:WorkspaceItem in zoomContainer.getComponents())
            	{
            		if (item is Node &&
            			ILayoutInfoItem(item).voUID == connector.sourceNodeUID)
            		{
            			newItem.fromNode = Node(item);
            			Node(item).connectionsOut.addItem(newItem);
            			break;
            		}
            	}
            }
            if (connector.destinationUID != null)
            {
            	for each (item in zoomContainer.getComponents())
            	{
            		if (item is Node &&
            			ILayoutInfoItem(item).voUID == connector.destinationUID)
            		{
            			newItem.toNode = Node(item);
            			Node(item).connectionsIn.addItem(newItem);
            			break;
            		}
            	}
            }
            addItemToWorkspace(newItem);
		}
		private function addItemToWorkspace(item:WorkspaceItem):void
		{
			// Bind readonly flag to items
			item.readonly = readonly;
			changeWatcherMap[item.uid] = BindingUtils.bindProperty(item, "readonly", this, "readonly");
			
			// Size item
			item.width = WORKSPACE_ITEM_WIDTH;
			item.height = WORKSPACE_ITEM_WIDTH;
			
			// Add item to workspace view pan zoom canvas
			if (item is Connection)
				zoomContainer.addComponentAtBottom(item);
			else
				zoomContainer.addComponent(item);
				
			item.addEventListener(MouseEvent.MOUSE_DOWN, workspaceItemMouseDown);
			item.addEventListener(MouseEvent.MOUSE_UP, workspaceItemMouseUp);
			
			item.addEventListener(WorkspaceEvent.BRING_TO_FRONT, onBringToFront);
			item.addEventListener(WorkspaceEvent.REMOVE_WORKSPACE_ITEM, onRemoveItem);
			item.addEventListener(WorkspaceEvent.SELECT_WORKSPACE_ITEM, onSelectItem);
			item.addEventListener(WorkspaceEvent.UNSELECT_ALL_WORKSPACE_ITEMS, onUnselectAllItems);
			item.addEventListener(WorkspaceEvent.UNSELECT_WORKSPACE_ITEM, onUnselectItem);
			item.addEventListener(WorkspaceEvent.CONNECT_ITEM, connectItem);
			item.addEventListener(WorkspaceEvent.CHANGE_CONNECTION_DESTINATION, changeConnectionDestination);
			item.addEventListener(WorkspaceEvent.CHANGE_CONNECTION_SOURCE, changeConnectionSource);
			
			item.addEventListener(WorkspaceEvent.OPEN_EDIT_MENU, onOpenEditMenu);
		}
		
		private function removeWorkspaceItem(item:WorkspaceItem):void
		{
			// Remove this item from connection lists
			if (item is Node)
			{
				var node:Node = item as Node;
			
				for each (var conn:Connection in node.connectionsIn)
				{
					if (conn.toNode == node)
						conn.toNode = null;
				}
				for each (conn in node.connectionsOut)
				{
					if (conn.fromNode == node)
						conn.fromNode = null;
				}
			}
			
			// Remove bindings
			changeWatcherMap[item.uid].unwatch();
			changeWatcherMap[item.uid] = null;
			
			// Remove event handlers
			item.removeEventListener(MouseEvent.MOUSE_DOWN, workspaceItemMouseDown);
			item.removeEventListener(MouseEvent.MOUSE_UP, workspaceItemMouseUp);
			item.removeEventListener(WorkspaceEvent.BRING_TO_FRONT, onBringToFront);
			item.removeEventListener(WorkspaceEvent.REMOVE_WORKSPACE_ITEM, onRemoveItem);
			item.removeEventListener(WorkspaceEvent.SELECT_WORKSPACE_ITEM, onSelectItem);
			item.removeEventListener(WorkspaceEvent.UNSELECT_ALL_WORKSPACE_ITEMS, onUnselectAllItems);
			item.removeEventListener(WorkspaceEvent.UNSELECT_WORKSPACE_ITEM, onUnselectItem);
			item.removeEventListener(WorkspaceEvent.CONNECT_ITEM, connectItem);
			item.removeEventListener(WorkspaceEvent.CHANGE_CONNECTION_DESTINATION, changeConnectionDestination);
			item.removeEventListener(WorkspaceEvent.CHANGE_CONNECTION_SOURCE, changeConnectionSource);
			
			item.removeEventListener(WorkspaceEvent.OPEN_EDIT_MENU, onOpenEditMenu);
			
			// Remove item from canvas
			zoomContainer.removeComponent(item);
		}

		//
		// Workspace item handlers
		//
		private function onBringToFront(e:WorkspaceEvent):void
		{
			bringToFront(e.workspaceItem);
		}
		private function onRemoveItem(e:WorkspaceEvent):void
		{
			deleteWorkspaceItem(e.workspaceItem);
		}
		private function onSelectItem(e:WorkspaceEvent):void
		{
			selectWorkspaceItem(e.workspaceItem);
		}
		private function onUnselectAllItems(e:WorkspaceEvent):void
		{
			unSelectAllWorkspaceItems();
		}
		private function onUnselectItem(e:WorkspaceEvent):void
		{
			unSelectWorkspaceItem(e.workspaceItem);
		}
		private function onOpenEditMenu(e:WorkspaceEvent):void
		{
			closeMenuItems();
			openWorkspaceItem = e.target as WorkspaceItem;
			openWorkspaceItem.openEditWindow();
		}
		
		/**
		 * Fire event to remove the workspace item from the server and
		 * remove the item from the workspace display list.
		 * 
		 * @param item WorkspaceItem
		 */
		public function deleteWorkspaceItem(item:WorkspaceItem):void
		{
			// Remove this item from connection lists
			if (item is Node)
			{
				var node:Node = item as Node;
				
				// Delete the item
				var event:CampaignEvent = new CampaignEvent(CampaignEvent.DELETE_NODE);
				event.node = node.nodeVO;
				dispatchEvent(event);
			}
			else if (item is Connection)
			{
				var connector:Connection = item as Connection;
				
				event = new CampaignEvent(CampaignEvent.DELETE_CONNECTOR);
				event.connector = connector.connectorVO;
				dispatchEvent(event);
			}
			removeWorkspaceItem(item);
		}
		public function deleteSelectedWorkspaceItems():void
		{
			for each (var item:WorkspaceItem in selectedWorkspaceItems)
			{
	            deleteWorkspaceItem(item);
			}
		}
		public function loadCampaign(campaign:CampaignVO):void
		{
			// Close any open menus
			closeMenuItems();
			
			// Remove any existing components
			for each (var item:WorkspaceItem in zoomContainer.getComponents())
				removeWorkspaceItem(item);
			
			// Add new nodes and connections
			if (campaign != null)
			{
				for each (var node:NodeVO in campaign.nodes)
					addNodeToWorkspace(node);
				
				for each (var connector:ConnectorVO in campaign.connectors)
					addConnectorToWorkspace(connector);
			}
		}
		
		public function alignLeft(e:WorkspaceEvent):void
		{
			if (selectedWorkspaceItems.length > 0)
			{
				var minX:Number = selectedWorkspaceItems[0].x;
				for each (var item:WorkspaceItem in selectedWorkspaceItems)
				{
					if (item.x < minX)
						minX = item.x;
				}
				for each (item in selectedWorkspaceItems)
				{
					item.x = minX;
				}
				saveLayout();
			}
		}
		public function alignRight(e:WorkspaceEvent):void
		{
			if (selectedWorkspaceItems.length > 0)
			{
				var maxX:Number = selectedWorkspaceItems[0].x;
				for each (var item:WorkspaceItem in selectedWorkspaceItems)
				{
					if (item.x > maxX)
						maxX = item.x;
				}
				for each (item in selectedWorkspaceItems)
				{
					item.x = maxX;
				}
				saveLayout();
			}
		}
		public function alignTop(e:WorkspaceEvent):void
		{
			if (selectedWorkspaceItems.length > 0)
			{
				var minY:Number = selectedWorkspaceItems[0].y;
				for each (var item:WorkspaceItem in selectedWorkspaceItems)
				{
					if (item.y < minY)
						minY = item.y;
				}
				for each (item in selectedWorkspaceItems)
				{
					item.y = minY;
				}
				saveLayout();
			}
		}
		public function alignBottom(e:WorkspaceEvent):void
		{
			if (selectedWorkspaceItems.length > 0)
			{
				var maxY:Number = selectedWorkspaceItems[0].y;
				for each (var item:WorkspaceItem in selectedWorkspaceItems)
				{
					if (item.y > maxY)
						maxY = item.y;
				}
				for each (item in selectedWorkspaceItems)
				{
					item.y = maxY;
				}
				saveLayout();
			}
		}
		
		//
		// Workspace item event handlers
		//
		private function workspaceItemMouseDown(e:MouseEvent):void
		{
			// If not in pan mode don't drag items
			if (!readonly &&
				selectedWorkspaceItems.contains(e.currentTarget))
				startDragSelected();
		}
		private function workspaceItemMouseUp(e:MouseEvent):void
		{
			stopDragSelected();
		}
		private function startDragSelected():void
		{
			addEventListener(MouseEvent.MOUSE_MOVE, doDragSelected);
		}
		private function stopDragSelected():void
		{
			removeEventListener(MouseEvent.MOUSE_MOVE, doDragSelected);
			lastMouseX = NaN;
			lastMouseY = NaN;
			saveLayout();
		}
		
		private function saveLayout():void
		{
			// Save final position of each selected item
			for each (var item:ILayoutInfoItem in selectedWorkspaceItems)
			{
				if (item.layoutInfo == null)
					item.layoutInfo = new LayoutInfoVO();
				item.layoutInfo.UUID = item.voUID;
				item.layoutInfo.campaignUUID = campaign.uid;
				item.layoutInfo.x = WorkspaceItem(item).x;
				item.layoutInfo.y = WorkspaceItem(item).y;
				
				var event:LayoutInfoEvent = new LayoutInfoEvent(LayoutInfoEvent.SAVE_LAYOUT_INFO);
				event.layoutInfo = item.layoutInfo;
				dispatchEvent(event);
			}
		}
		private function doDragSelected(e:MouseEvent):void
		{
			if (!e.buttonDown)
				stopDragSelected();
				
			if (!isNaN(lastMouseX) &&
				!isNaN(lastMouseY))
			{
				for each (var item:WorkspaceItem in selectedWorkspaceItems)
				{
					var localNew:Point = item.parent.globalToLocal(
						new Point(Application.application.mouseX, Application.application.mouseY));
					var localOld:Point = item.parent.globalToLocal(
						new Point(lastMouseX, lastMouseY));
					
					item.x += localNew.x - localOld.x;
					item.y += localNew.y - localOld.y;
				}
			}
			lastMouseX = Application.application.mouseX;
			lastMouseY = Application.application.mouseY;
		}
		public function bringToFront(item:WorkspaceItem):void
		{
			// Add the item as the last item in the display list to
			// bring to front, unless it is a connection
			zoomContainer.removeComponent(item);
			zoomContainer.addComponent(item);
		}
		public function closeMenuItems():void
		{
			if (openWorkspaceItem != null)
				openWorkspaceItem.closeEditWindow();
		}
		
		//
		// Selection methods
		//
		public function selectWorkspaceItemByUID(uid:String):void
		{
			for each (var item:ILayoutInfoItem in zoomContainer.getComponents())
			{
				if (item.voUID == uid)
				{
					selectWorkspaceItem(item as WorkspaceItem);
					break;
				}
			}
		}
		public function selectWorkspaceItem(item:WorkspaceItem):void
		{
			selectWorkspaceItems([item]);
		}
		public function selectWorkspaceItems(items:Array):void
		{
			if (items != null)
			{
				for each (var item:WorkspaceItem in items)
				{
					item.selected = true;
					if (!selectedWorkspaceItems.contains(item))
						selectedWorkspaceItems.addItem(item);
				}
			}
		}
		public function selectSelectionBoxItems():void
		{
			unSelectAllWorkspaceItems();
			for each (var item:WorkspaceItem in zoomContainer.getComponents())
			{
				var global:Point = item.parent.localToGlobal(new Point(item.x, item.y));
				var local:Point = globalToLocal(global);
				if (selectionBox.containsCoordinate(local) )
				{
					selectWorkspaceItem(item);
				}
			}
			selectionBox.stopSelection();
		}
		public function unSelectWorkspaceItem(item:WorkspaceItem):void
		{
			unSelectWorkspaceItems([item]);
		}
		public function unSelectAllWorkspaceItems():void
		{
			unSelectWorkspaceItems(selectedWorkspaceItems.toArray());
		}
		public function unSelectWorkspaceItems(items:Array):void
		{
			if (items != null)
			{
				for each (var item:WorkspaceItem in items)
				{
					item.selected = false;
					if (selectedWorkspaceItems.contains(item))
						selectedWorkspaceItems.removeItemAt(
							selectedWorkspaceItems.getItemIndex(item));
				}
			}
		}	
	
		//
		// Connection methods
		//
        private function endConnection():void
		{
			if (sourceConnection != null)
			{
				// Save connection changes
				var event:CampaignEvent = new CampaignEvent(CampaignEvent.SAVE_CONNECTOR);
				event.connector = sourceConnection.editConnectorVO;
				dispatchEvent(event);
				
				// Change mode
				sourceConnection.mode = Connection.MODE_CONNECT_ENDS;
				sourceConnection.redrawLine();
				sourceConnection = null;
				
			}
			if (destinationConnection != null)
			{
				
				// Save connection changes
				event = new CampaignEvent(CampaignEvent.SAVE_CONNECTOR);
				event.connector = destinationConnection.editConnectorVO;
				dispatchEvent(event);
				
				// Change mode
				destinationConnection.mode = Connection.MODE_CONNECT_ENDS;
				destinationConnection.redrawLine();
				destinationConnection = null;
			}
		}
		public function changeConnectionSource(e:WorkspaceEvent):void
		{
			sourceConnection = e.target as Connection;
			sourceConnection.mode = Connection.MODE_CONNECT_SOURCE;
			
			// Remove any previous connection source
			if (sourceConnection.fromNode != null)
			{
				sourceConnection.fromNode.connectionsOut.removeItemAt(
					sourceConnection.fromNode.connectionsOut.getItemIndex(sourceConnection));
				sourceConnection.fromNode = null;
			}
			sourceConnection.editConnectorVO.sourceNodeUID = null;
		}
		public function changeConnectionDestination(e:WorkspaceEvent):void
		{
			destinationConnection = e.target as Connection;
			destinationConnection.mode = Connection.MODE_CONNECT_DESTINATION;
			
			// Remove any previous connection destination
			if (destinationConnection.toNode != null)
			{
				destinationConnection.toNode.connectionsIn.removeItemAt(
					destinationConnection.toNode.connectionsIn.getItemIndex(destinationConnection));
				destinationConnection.toNode = null;
			}
			destinationConnection.editConnectorVO.destinationUID = null;
		}
		public function connectItem(e:WorkspaceEvent):void
		{
			var node:Node = e.target as Node;
			
			// Only try to connect the target if it is a Node
			if (node != null)
			{
				bringToFront(node);
				
				// If setting a source connection connect it
				if (sourceConnection != null)
				{
					node.connectionsOut.addItem(sourceConnection);
					sourceConnection.fromNode = node;
					
					// Set VO data
					sourceConnection.editConnectorVO.sourceNodeUID = node.editNodeVO.uid;
					
				}
				// If setting a destination connection connect it
				else if (destinationConnection != null)
				{
					node.connectionsIn.addItem(destinationConnection);
					destinationConnection.toNode = node;
					
					// Set VO data
					destinationConnection.editConnectorVO.destinationUID = node.editNodeVO.uid;
					
				}
			}
			
			endConnection();
		}	
	}
}