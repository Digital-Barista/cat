package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.OutgoingEntryNode")]
	public class OutgoingEntryPointVO extends NodeVO
	{
		public function OutgoingEntryPointVO()
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
					if (ed.entryType != null)
						return true;
				}
			}
			return false;
		}
	}
}