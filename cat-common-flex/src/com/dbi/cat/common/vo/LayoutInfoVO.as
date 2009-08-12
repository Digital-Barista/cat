package com.dbi.cat.common.vo
{
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.LayoutInfo")]
	public class LayoutInfoVO
	{
		public function LayoutInfoVO()
		{
		}
		
		public var UUID:String;
		public var campaignUUID:String;
		public var x:Number = 0;
		public var y:Number = 0;
		public var version:Number;
		
		public function get key():String
		{
			return UUID + ":" + version;
		}
	}
}