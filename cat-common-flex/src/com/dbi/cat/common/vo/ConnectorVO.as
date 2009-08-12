package com.dbi.cat.common.vo
{
	import mx.utils.UIDUtil;
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.Connector")]
	public class ConnectorVO
	{
		public function ConnectorVO()
		{
		}
		
		public var sourceNodeUID:String;
		public var destinationUID:String;
		
		public var campaignUID:String;
		public var name:String;
		public var uid:String = UIDUtil.createUID();
		
		public var layoutInfo:LayoutInfoVO;
		
		[Transient]
		public function get valid(): Boolean
		{
			return true;
		}
	}
}