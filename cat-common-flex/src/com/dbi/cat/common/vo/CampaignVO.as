package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	import mx.utils.UIDUtil;
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.Campaign")]
	public class CampaignVO
	{
		public function CampaignVO()
		{
		}

		public var primaryKey:Number;
		public var name:String;
		public var uid:String = UIDUtil.createUID();
		public var currentVersion:Number = 0;
		public var nodes:ArrayCollection;
		public var connectors:ArrayCollection;
		public var clientPK:Number;
		public var type:String;
		public var defaultFromAddress:String;
		public var addInMessage:String;
	}
}