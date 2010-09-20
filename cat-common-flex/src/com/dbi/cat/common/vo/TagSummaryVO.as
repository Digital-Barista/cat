package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.reporting.TagSummary")]
	public class TagSummaryVO
	{
		public function TagSummaryVO()
		{
			super();
		}
		
		public var clientId:Number;
		public var clientName:String;
		public var tag:String;
		public var entryPointType:String;
		public var userCount:Number;
	}
}