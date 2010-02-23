package com.dbi.util
{
	import com.dbi.controls.CustomMessage;
	
	import mx.collections.ArrayCollection;
	import mx.events.ValidationResultEvent;
	import mx.validators.Validator;
	
	public class ValidationUtil
	{
		public function ValidationUtil()
		{
		}

		public static function reportErrors(validators:Array):Boolean
		{
			var errors:Array = Validator.validateAll(validators);
		
			var errorMessage:String = "";	
			for each (var result:ValidationResultEvent in errors)
			{
				if (errorMessage.length > 0)
					errorMessage += "\n";
				errorMessage += result.message;
			}
			
			if (errorMessage.length > 0)
				CustomMessage.show(errorMessage);

			return errors.length == 0;
		}
	}
}