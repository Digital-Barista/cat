package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.CampaignMessagePart")]
	public class CampaignMessagePartVO
	{
		public function CampaignMessagePartVO()
		{
		}

		public var entryType:String;
		public var messages:ArrayCollection;
	}
}