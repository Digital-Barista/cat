package com.dbi.cat.common.vo
{
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.ImmediateConnector")]
	public class ImmediateConnectorVO extends ConnectorVO
	{
		public var type:String;
		
		public function ImmediateConnectorVO()
		{
			super();
		}
		
	}
}