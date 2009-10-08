package com.dbi.cat.common.constants
{
	public class EntryPointType
	{
		public function EntryPointType()
		{
		}

		public static const EMAIL:String = "Email";
		public static const SMS:String = "SMS";
		public static const TWITTER:String = "Twitter";
		
		public static const EMAIL_MAX_CHARACTERS:Number = Number.POSITIVE_INFINITY;
		public static const SMS_MAX_CHARACTERS:Number = 160;
		public static const TWITTER_MAX_CHARACTERS:Number = 140;
		
		public static function getMaxCharacters(entryPointType:String):Number
		{
			switch(entryPointType)
			{
				case EMAIL:
					return EMAIL_MAX_CHARACTERS;
					break;
				case SMS:
					return SMS_MAX_CHARACTERS;
					break;
				case TWITTER:
					return TWITTER_MAX_CHARACTERS;
					break;
				default:
					return 0;
					break;
			}
		}
	}
}