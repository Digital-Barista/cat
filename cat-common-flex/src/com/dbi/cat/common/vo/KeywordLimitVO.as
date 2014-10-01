package com.dbi.cat.common.vo
{
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.KeywordLimit")]
	public class KeywordLimitVO
	{
		
		public var keywordLimitId:String;
		public var clientId:Number;
		public var entryType:String;
		public var maxKeywords:Number = 5;
		
	
		public function KeywordLimitVO()
		{
		}
	}
}