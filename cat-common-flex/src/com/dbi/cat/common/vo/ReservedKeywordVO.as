package com.dbi.cat.common.vo
{
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.ReservedKeyword")]
	public class ReservedKeywordVO
	{
		
		public var reservedKeywordId:String;
		public var keyword:String;
		
	
		public function ReservedKeywordVO()
		{
		}
	}
}