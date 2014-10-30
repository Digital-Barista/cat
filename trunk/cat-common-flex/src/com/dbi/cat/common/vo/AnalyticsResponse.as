package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;

	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.reporting.AnalyticsResponse")]
	public class AnalyticsResponse
	{
		public var startDate:Date;
		public var endDate:Date;
		public var maxCount:Number = 0;
		public var dataList:ArrayCollection;		
		
		public function AnalyticsResponse()
		{
			super();
		}
	}
}