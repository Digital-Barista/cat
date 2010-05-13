package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.reporting.DashboardCount")]
	public class DashboardCountVO
	{
		public function DashboardCountVO()
		{
			super();
		}
		
		public var entryPointType:String;
		public var count:Number;
	}
}