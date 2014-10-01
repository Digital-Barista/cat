package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.ResponseConnector")]
	public class ResponseConnectorVO extends ConnectorVO
	{
		public function ResponseConnectorVO()
		{
			super();
		}
		
		public var entryData:ArrayCollection = new ArrayCollection();
		
		override public function get valid():Boolean
		{
			if (entryData != null)
			{
				for each (var ed:EntryDataVO in entryData)
				{
					if (ed.valid)
						return true;
				}
			}
			return false;
		}
	}
}