package com.dbi.cat.view.workspace
{
	import com.dbi.cat.common.vo.LayoutInfoVO;
	import com.dbi.cat.common.vo.NodeVO;
	import com.dbi.cat.event.CampaignEvent;
	import com.dbi.cat.event.WorkspaceEvent;
	
	import flash.events.Event;
	
	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.utils.ObjectUtil;
	
	public class Node extends WorkspaceItem
	{
		protected var statisticsComponent:StatisticsComponent;
		
		/**
		 * Returns the layout infor from the NodeVO
		 */
		public function get layoutInfo():LayoutInfoVO
		{
			return nodeVO.layoutInfo;
		}
		public function set layoutInfo(info:LayoutInfoVO):void
		{
			nodeVO.layoutInfo = info;
		}
		
		public function get voUID():String
		{
			return nodeVO.uid;
		}
			
		/**
		 * Flag to determine if statistics icon should show
		 */
		 public var showStatistics:Boolean = true;
		 
		public function Node()
		{
			super();
			
			addEventListener(WorkspaceEvent.CLOSE_EDIT_MENU, onCloseMenu);
			
			// Bind invalid warning icon to invalid property
			BindingUtils.bindProperty(invalidWarningLoader, "visible", this, "inValid");
			
			// Setup statistics component
			statisticsComponent = new StatisticsComponent();
			statisticsComponent.setStyle("left", 0);
			statisticsComponent.setStyle("bottom", 0);
			
		}
		
		
		private function onCloseMenu(e:WorkspaceEvent):void
		{
			// Save this VO whenever the menu is closed
			var event:CampaignEvent = new CampaignEvent(CampaignEvent.SAVE_NODE);
			event.node = editNodeVO;
			dispatchEvent(event);
			
			// Dispatch event to update validation
			dispatchEvent(new Event("checkValidState"));
		}
		
		
		// Connection properties
		private var _connectionsIn:ArrayCollection = new ArrayCollection();
		public function get connectionsIn():ArrayCollection
		{
			return _connectionsIn
		}
		public function set connectionsIn(connections:ArrayCollection):void
		{
			_connectionsIn = connections;
		}
		
		private var _connectionsOut:ArrayCollection = new ArrayCollection();
		public function get connectionsOut():ArrayCollection
		{
			return _connectionsOut
		}
		public function set connectionsOut(connections:ArrayCollection):void
		{
			_connectionsOut = connections;
		}
		
		// VO data
		private var _nodeVO:NodeVO;
		[Bindable]
		public function get nodeVO():NodeVO
		{
			return _nodeVO;
		}
		public function set nodeVO(value:NodeVO):void
		{
			_nodeVO = value;
			editNodeVO = ObjectUtil.copy(nodeVO) as NodeVO;
			BindingUtils.bindProperty(this, "title", editNodeVO, "name");
			BindingUtils.bindProperty(statisticsComponent, "data", nodeVO, "subscriberCount");
			dispatchEvent(new Event("checkValidState"));
			dispatchEvent(new Event("nodeUpdate"));
		}
		
		public var editNodeVO:NodeVO;
		
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
		
		override protected function childrenCreated():void
		{
			super.childrenCreated();
			
			if (showStatistics)
				addChild(statisticsComponent);
		}
	}
}