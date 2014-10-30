package com.dbi.cat.common.vo
{
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.CalendarConnector")]
	public class CalendarConnectorVO extends ConnectorVO
	{
		public var targetDate:Date;
		public var type:String;
		
		public function CalendarConnectorVO()
		{
			super();
			var now:Date = new Date();
			targetDate = new Date(now.fullYear, now.month, now.date);
		}
		override public function get valid():Boolean
		{
			return targetDate != null;
		}
		
	}
}