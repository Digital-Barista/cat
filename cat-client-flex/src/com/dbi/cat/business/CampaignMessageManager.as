package com.dbi.cat.business
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	public class CampaignMessageManager
	{
		public var messageParts:ArrayCollection;
		
		public function CampaignMessageManager()
		{
		}

		public function loadMessageParts(parts:ArrayCollection):void
		{
			messageParts = parts;
		}
	}
}