package com.dbi.cat.common.constants
{
	import mx.collections.ArrayCollection;
	
	[Bindable]
	public class EntryPointType
	{
		public var name:String;
		public var maxCharacters:Number;
		public var defaultMessageCredits:Number; // NaN = Unlimited
		public var allowHTML:Boolean = false;
		
		private static var _allTypes:ArrayCollection;
		
		public function get allowAutoStart():Boolean
		{
			if (this == TWITTER ||
				this == FACEBOOK)
				return true;
			return false;
		}
		
		public function EntryPointType(name:String, maxChars:Number, 
									   defaultMessageCredits:Number=NaN, allowHTML:Boolean=false)
		{
			this.name = name;
			this.maxCharacters = maxChars;
			this.defaultMessageCredits = defaultMessageCredits;
			this.allowHTML = allowHTML;
		}

		public static const EMAIL:EntryPointType = new EntryPointType("Email", 0, NaN, true);
		public static const SMS:EntryPointType = new EntryPointType("SMS", 160, 0);
		public static const TWITTER:EntryPointType = new EntryPointType("Twitter", 140);
		public static const FACEBOOK:EntryPointType = new EntryPointType("Facebook", 0, NaN, true);
		
		
		public static function allowAutoStart(entryType:String):Boolean
		{
			for each (var type:EntryPointType in allTypes)
			{
				if (type.name == entryType)
					return type.allowAutoStart;
			}
			return false;
		}
		
		
		public static function get allTypes():ArrayCollection
		{
			if (_allTypes == null)
			{
				_allTypes = new ArrayCollection([
				EntryPointType.SMS,
				EntryPointType.EMAIL,
				EntryPointType.TWITTER,
				EntryPointType.FACEBOOK]);
			}
			return _allTypes;
		}
	}
}