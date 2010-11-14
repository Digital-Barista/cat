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
		public var messageSendDates:ArrayCollection;
		public var facebookAppVisits:ArrayCollection;
		
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
			this.contactCreateDates = fillEmptyDays(creates, start, end);
		}
		
		public function loadMessageSendDates(messageDates:ArrayCollection, start:Date, end:Date):void
		{
			this.messageSendDates = fillEmptyDays(messageDates, start, end);
		}
		
		public function loadFacebookAppVisits(visits:ArrayCollection, start:Date, end:Date):void
		{
			this.facebookAppVisits = fillEmptyDays(visits, start, end);;
		}
		
		/**
		 * Take a list of DateData and fill a new collection with 
		 * DateData for every day in a given range creating new ones
		 * for the days missing in the dateData list given
		 * 
		 * @param dateData Data where counts are non zero
		 * @param start Start date of the range to fill
		 * @param end End date of the range to fill
		 * @return A new ArrayCollection with DateData for each day in range
		 */
		private function fillEmptyDays(dateData:ArrayCollection, start:Date, end:Date):ArrayCollection
		{
			// Fill in missing dates with zero counts
			var ret:ArrayCollection = new ArrayCollection();
			
			// Map the current data by date
			var dateMap:Object = new Object();
			for each (var data:DateData in dateData)
				dateMap[data.date.fullYear + "-" + data.date.month + "-" + data.date.date] = data;
			
			// Go through every day in range
			var time:Number = start.time;
			while (time < end.time)
			{
				var d:Date = new Date(time);
				var existing:DateData = dateMap[d.fullYear + "-" + d.month + "-" + d.date];
				
				if (existing != null)
				{
					ret.addItem(existing);
				}
				else
				{
					var empty:DateData = new DateData();
					empty.date = d;
					empty.count = 0;
					if (dateData.length > 0)
						empty.total = dateData.getItemAt(0).total;
					ret.addItem(empty);
				}
				
				time += 24*60*60*1000;
			}
			
			return ret;
		}
	}
}