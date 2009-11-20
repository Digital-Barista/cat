package com.dbi.util
{
	import mx.formatters.DateFormatter;
	
	/**
	 * This class is used to perform date manipulation that is
	 * not provided by the basic Actionscript libraries
	 *  
	 * @author khoyt2
	 * 
	 */	
	public class DateUtil
	{
		public function DateUtil()
		{
		}

		public static function formatDate(date:Object):String
		{
			var df:DateFormatter = new DateFormatter();
			df.formatString = "YYYY-MM-DD";
			return df.format(date);	
		}
		
		/**
		 * Take a date string from a service response and parse it
		 * into an actionscript date object
		 *  
		 * @param value - String of date to parse
		 * @return - New date object
		 * 
		 */
		public static function parseFromServiceDate(value:String):Date
		{
			var ret:Date = null;
			
			if (value != null)
			{
				var dateString:String = value;
	            dateString = dateString.replace(/-/g, "/");
	            dateString = dateString.replace("T", " ");
	            dateString = dateString.replace("Z", " GMT-0000");
	            ret = new Date(Date.parse(dateString));
		   }
		   return ret;
		}
		
		/**
		 * Returns the UTC representation of the date provided as
		 * a String
		 * 
		 * @param date - Date to format
		 * 
		 * @return String representing the date in UTC formatted for a service call
		 */
		public static function parseToServiceDate(date:Date):String
		{
			var ret:String = null;
			if (date != null)
			{
				var df:DateFormatter = new DateFormatter();
				df.formatString = "YYYY-MM-DDTJJ:NN:SSZ";
				ret = df.format(date.toUTCString());
			}
			
			return ret;
		}
	}
}