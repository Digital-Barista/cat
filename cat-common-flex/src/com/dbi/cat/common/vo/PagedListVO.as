package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.util.PagedList")]
	public class PagedListVO
	{
		public function PagedListVO()
		{
			super();
			
			results = new ArrayCollection();
		}
		
		public var totalResultCount:Number;
		public var results:ArrayCollection;
		
	}
}