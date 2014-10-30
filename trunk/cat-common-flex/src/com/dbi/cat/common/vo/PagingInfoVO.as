package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.PagingInfo")]
	public class PagingInfoVO
	{
		public function PagingInfoVO()
		{
			super();
		}
		
		public var pageIndex:String;
		public var pageSize:String;
		public var sortProperty:String;
		public var sortDirectionAscending:Boolean;
		
	}
}