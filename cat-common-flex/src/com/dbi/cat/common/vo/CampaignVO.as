package com.dbi.cat.common.vo
{
	import com.dbi.cat.common.constants.CampaignMode;
	
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
		public var mode:String = CampaignMode.NORMAL;
		public var uid:String = UIDUtil.createUID();
		public var currentVersion:Number = 0;
		public var nodes:ArrayCollection;
		public var connectors:ArrayCollection;
		public var clientPK:Number;
		public var addInMessages:ArrayCollection;
		public var campaignInfos:ArrayCollection;
		public var isAutoStart:Boolean;
		public var subscriberCount:Number = 0;
	}
}