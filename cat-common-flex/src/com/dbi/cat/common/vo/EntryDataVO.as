package com.dbi.cat.common.vo
{
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.EntryData")]
	public class EntryDataVO
	{
		public var entryType:String;
		public var entryPoint:String;
		public var keyword:String;
		
		public function EntryDataVO()
		{
			super();
		}
		
		public function get valid():Boolean
		{
			if (entryType != null &&
				entryType.length > 0 &&
				entryPoint != null &&
				entryPoint.length > 0 &&
				keyword != null &&
				keyword.length > 0)
			{
				return true;
			}
			return false;
		}
		
	}
}