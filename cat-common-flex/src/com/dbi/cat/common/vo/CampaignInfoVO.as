package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.CampaignInfo")]
	public class CampaignInfoVO
	{
		public static const AUTO_START_NODE_UID:String = "autoStartNodeUID";
		
		public function CampaignInfoVO()
		{
			super();
		}
		
		public var campaignInfoId:String;
		public var campaignId:String;
		public var entryType:String;
		public var name:String;
		public var value:String;
	}
}