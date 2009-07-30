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
		
		public var entryPoint:String;
		public var entryType:String;
		public var keyword:String;
		
		override public function get valid():Boolean
		{
			return entryPoint != null &&
				entryPoint.length > 0 &&
				entryType != null &&
				entryType.length > 0 &&
				keyword != null &&
				keyword.length > 0;
		}
	}
}