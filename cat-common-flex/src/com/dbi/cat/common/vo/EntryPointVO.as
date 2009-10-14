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
//		public var entryPoints:ArrayCollection = new ArrayCollection();
//		public var entryTypes:ArrayCollection = new ArrayCollection();
//		public var keywords:ArrayCollection = new ArrayCollection();
		
		override public function get valid():Boolean
		{
			return entryData != null &&
				entryData.length > 0;
//			return entryPoints != null &&
//				entryPoints.length > 0 &&
//				entryTypes != null &&
//				entryTypes.length > 0 &&
//				keywords != null &&
//				keywords.length > 0;
		}
	}
}