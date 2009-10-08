package com.dbi.cat.common.vo
{
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.EntryNode")]
	public class EntryPointVO extends NodeVO
	{
		public function EntryPointVO()
		{
			super();
		}
		
		public var entryPoints:Array = new Array();
		public var entryTypes:Array = new Array();
		public var keywords:Array = new Array();
		
		override public function get valid():Boolean
		{
			return entryPoints != null &&
				entryPoints.length > 0 &&
				entryTypes != null &&
				entryTypes.length > 0 &&
				keywords != null &&
				keywords.length > 0;
		}
	}
}