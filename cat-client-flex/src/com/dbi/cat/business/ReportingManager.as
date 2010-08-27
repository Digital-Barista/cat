package com.dbi.cat.business
{
	import com.dbi.cat.common.vo.DashboardDataVO;
	import com.dbi.cat.common.vo.DateData;
	import com.dbi.cat.event.ReportingEvent;
	import com.dbi.cat.view.reporting.NodeSubscriberAddressView;
	
	import flash.display.DisplayObject;
	import flash.events.IEventDispatcher;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IViewCursor;
	import mx.core.Application;
	import mx.core.IFlexDisplayObject;
	import mx.managers.PopUpManager;
	import mx.utils.ObjectUtil;
	
	[Bindable]
	public class ReportingManager
	{
		private var dispatcher:IEventDispatcher;
		
		public var dashboardData:DashboardDataVO;
		public var outgoingMessageSummaries:ArrayCollection;
		public var tagSummaries:ArrayCollection;
		public var contactCreateDates:ArrayCollection;
		
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
		public function loadContactCreateDates(creates:ArrayCollection, start:Date, end:Date):void
		{
			// Fill in missing dates with zero counts
			this.contactCreateDates = new ArrayCollection();
			
			// Map the current data by date
			var dateMap:Object = new Object();
			for each (var data:DateData in creates)
				dateMap[data.date.fullYear + "-" + data.date.month + "-" + data.date.date] = data;
			
			// Go through every day in range
			var time:Number = start.time;
			while (time < end.time)
			{
				var d:Date = new Date(time);
				var existing:DateData = dateMap[d.fullYear + "-" + d.month + "-" + d.date];
				
				if (existing != null)
				{
					this.contactCreateDates.addItem(existing);
				}
				else
				{
					var empty:DateData = new DateData();
					empty.date = d;
					empty.count = 0;
					if (creates.length > 0)
						empty.total = creates.getItemAt(0).total;
					this.contactCreateDates.addItem(empty);
				}
				
				time += 24*60*60*1000;
			}
		}
	}
}