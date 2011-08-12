package com.dbi.cat.common.vo
{
	import com.dbi.cat.common.constants.CampaignMode;
	
	import mx.collections.ArrayCollection;
	import mx.utils.UIDUtil;
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.CampaignEntryMessage")]
	public class CampaignEntryMessageVO
	{
		public function CampaignEntryMessageVO()
		{
		}
		
		public var messageNode:NodeVO;
		public var active:Boolean;
		public var entryData:ArrayCollection;
	}
}