package com.dbi.cat.common.vo
{
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.ResponseConnector")]
	public class ResponseConnectorVO extends ConnectorVO
	{
		public function ResponseConnectorVO()
		{
			super();
		}
		
		public var entryPoint:String;
		public var entryPointType:String;
		public var keyword:String;
		
			override public function get valid():Boolean
			{
				return entryPoint != null &&
					entryPoint.length > 0 &&
					entryPointType != null &&
					entryPointType.length > 0 &&
					keyword != null &&
					keyword.length > 0;
			}
	}
}