package com.dbi.cat.common.vo
{
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.reporting.AnalyticsData")]
	public class AnalyticsData extends DateData
	{
		public var visits:Number = 0;
		public var newVisits:Number = 0;
		public var timeOnSite:Number = 0;
		
		
		public function AnalyticsData()
		{
			super();
		}
	}
}