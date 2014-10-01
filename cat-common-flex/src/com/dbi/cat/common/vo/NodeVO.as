package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	import mx.utils.UIDUtil;
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.Node")]
	public class NodeVO
	{
		public function NodeVO()
		{
		}
		
		public var downstreamConnections:ArrayCollection = new ArrayCollection();
		public var upstreamConnections:ArrayCollection = new ArrayCollection();
		
		public var campaignUID:String;
		public var name:String = "";
		public var uid:String = UIDUtil.createUID();
		
		public var layoutInfo:LayoutInfoVO;
		public var subscriberCount:Number = 0;
		
		[Transient]
		public function get valid(): Boolean
		{
			return true;
		}
	}
}