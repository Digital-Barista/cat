package com.dbi.util
{
	import com.dbi.event.CustomMessageEvent;
	import com.dbi.controls.CustomMessage;
	
	import mx.collections.ArrayCollection;
	import mx.logging.ILogger;
	import mx.logging.Log;
	import mx.logging.LogEventLevel;
	
	
	/**
	 * This class can be used to log errors as well
	 * as report error conditions to the front end
	 *  
	 * @author khoyt2
	 * 
	 */	
	public class LogUtil
	{
		public static var preventPopupMessages:Boolean = false;
		private static var openMessages:ArrayCollection = new ArrayCollection();
		
		public function LogUtil()
		{
		}
		
		/**
		 * This will check a service ResultEvent to see if an error
		 * was returned and if an authentication error is detected will
		 * clear data out of the model and logout the user.
		 *  
		 * @param result - Object (usually a ResultEvent.result) returned from successful service call
		 * @returns true if an authentication error was detected
		 */		
		public static function HandleError(e:Object):Boolean
		{
			var hasError:Boolean = false;
			
			if (e == null ||
				e.length == 0)
			{
				hasError = true;
				Report("The server is not responding properly");
			}
			else if (e != null &&
				e.hasOwnProperty("serviceResponse") &&
				!e.serviceResponse.success)
			{
				hasError = true;
				var errorId:Number;
				
				if (e.serviceResponse.hasOwnProperty("errorId"))
				  errorId = e.serviceResponse.errorId;
				
				// Report error message
				Report(e.serviceResponse.message);
			}
			return hasError;
		}
		
		/**
		 * Log messages and errors if provided
		 *  
		 * @param message String to write to log
		 * @param error Error that was thrown and needs to be logged
		 * 
		 */
		public static function LogMessage(message:String, level:int=2, error:Error=null):void
		{
			var logger:ILogger = Log.getLogger("LogUtil");
			logger.log(level, message);
		}
		
		public static function LogDebug(message:String):void
		{
			LogMessage(message, LogEventLevel.DEBUG);
		}
		public static function LogError(message:String, error:Error):void
		{
			LogMessage(message, LogEventLevel.ERROR, error);
		}
		
		/**
		 * Log the message and error and then throw an Alert if
		 * an alert with the same message is not already open
		 *  
		 * @param message Text to display in an Alert
		 * @param error Error that will be logged
		 * 
		 */		
		public static function Report(message:String, error:Error=null):void
		{
			if (error != null)
				LogDebug(message);
			else
				LogError(message, error);
			
			// Don't throw the same Alert message if it is open already
			if (!openMessages.contains(message))
			{
				openMessages.addItem(message);
				
				// Logutil can be set to prevent showing actual popups
				if (!preventPopupMessages)
					CustomMessage.show(message, ["OK"], closeAlert);
			}
		}
		
		/**
		 * Remove the message from the list when the Alert
		 * is closed
		 *  
		 * @param e CloseEvent with click response
		 * 
		 */		
		private static function closeAlert(e:CustomMessageEvent):void
		{
			// Remove message from list
			if (openMessages.contains(e.Message))
				openMessages.removeItemAt(
					openMessages.getItemIndex(e.Message));
		}
	
	}
}