package com.dbi.cat.common.vo
{
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.IntervalConnector")]
	public class IntervalConnectorVO extends ConnectorVO
	{
		public function IntervalConnectorVO()
		{
			super();
		}
		
		public var interval:Number;
		public var intervalType:String;
		public var type:String;
		
			override public function get valid():Boolean
			{
				return !isNaN(interval) &&
					interval > 0 &&
					intervalType != null &&
					intervalType.length > 0;
			}
	}
}