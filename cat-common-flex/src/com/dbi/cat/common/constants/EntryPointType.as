package com.dbi.cat.common.constants
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	public class EntryPointType
	{
		public var name:String;
		public var maxCharacters:Number;
		public var defaultMessageCredits:Number; // NaN = Unlimited
		
		
		public function get allowAutoStart():Boolean
		{
			if (this == TWITTER ||
				this == FACEBOOK)
				return true;
			return false;
		}
		
		public function EntryPointType(name:String, maxChars:Number, defaultMessageCredits:Number=NaN)
		{
			this.name = name;
			this.maxCharacters = maxChars;
			this.defaultMessageCredits = defaultMessageCredits;
		}

		public static const EMAIL:EntryPointType = new EntryPointType("Email", 0);
		public static const SMS:EntryPointType = new EntryPointType("SMS", 160, 0);
		public static const TWITTER:EntryPointType = new EntryPointType("Twitter", 140);
		public static const FACEBOOK:EntryPointType = new EntryPointType("Facebook", 0);
		
		
		public static function allowAutoStart(entryType:String):Boolean
		{
			for each (var type:EntryPointType in getAllTypes())
			{
				if (type.name == entryType)
					return type.allowAutoStart;
			}
			return false;
		}
		
		
		public static function getAllTypes():ArrayCollection
		{
			return new ArrayCollection([
				EntryPointType.SMS,
				EntryPointType.EMAIL,
				EntryPointType.TWITTER,
				EntryPointType.FACEBOOK]);
		}
	}
}