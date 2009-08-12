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
		}
		override public function get valid():Boolean
		{
			return targetDate != null;
		}
		
	}
}