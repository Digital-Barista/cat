package com.dbi.cat.business
{
	import com.dbi.cat.common.vo.DashboardDataVO;
	import com.dbi.cat.event.ReportingEvent;
	import com.dbi.cat.view.reporting.NodeSubscriberAddressView;
	
	import flash.display.DisplayObject;
	import flash.events.IEventDispatcher;
	
	import mx.collections.ArrayCollection;
	import mx.core.Application;
	import mx.core.IFlexDisplayObject;
	import mx.managers.PopUpManager;
	
	[Bindable]
	public class ReportingManager
	{
		private var dispatcher:IEventDispatcher;
		
		public var dashboardData:DashboardDataVO;
		public var outgoingMessageSummaries:ArrayCollection;
		public var tagSummaries:ArrayCollection;
		
		public function ReportingManager(dispatcher:IEventDispatcher)
		{
			this.dispatcher = dispatcher;
		}

		//
		// Message reporting methods
		//
		public function loadOutgoingMessageSummaries(summaries:ArrayCollection):void
		{
			outgoingMessageSummaries = summaries;
		}
		public function loadDashboardData(dashboardData:DashboardDataVO):void
		{
			this.dashboardData = dashboardData;
		}
		public function loadTagSummaries(summaries:ArrayCollection):void
		{
			this.tagSummaries = summaries;
		}
	}
}