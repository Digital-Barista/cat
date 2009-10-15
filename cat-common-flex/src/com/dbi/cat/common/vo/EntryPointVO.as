package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.EntryNode")]
	public class EntryPointVO extends NodeVO
	{
		public function EntryPointVO()
		{
			super();
		}
		
		public var entryData:ArrayCollection = new ArrayCollection();
		
		override public function get valid():Boolean
		{
			return entryData != null &&
				entryData.length > 0;
		}
	}
}