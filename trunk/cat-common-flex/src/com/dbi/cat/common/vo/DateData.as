package com.dbi.cat.common.vo
{
	
	import mx.formatters.DateFormatter;

	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.reporting.DateData")]
	public dynamic class DateData
	{
		public var year:Number;
		public var month:Number;
		public var day:Number;
		public var count:Number = 0;
		public var total:Number = 0;
		public var date:Date;
		
		public function DateData()
		{
		}
	}
}