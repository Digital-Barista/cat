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
		
	}
}