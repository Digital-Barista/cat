package com.dbi.cat.common.vo
{
	
	import mx.formatters.DateFormatter;

	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.reporting.DateData")]
	public class DateData
	{
		public var year:Number;
		public var month:Number;
		public var day:Number;
		public var date:Date;
		public var count:Number;
		public var total:Number;
		
		
		public function DateData()
		{
		}
	}
}