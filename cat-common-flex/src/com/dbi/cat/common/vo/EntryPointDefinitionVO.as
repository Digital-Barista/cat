package com.dbi.cat.common.vo
{
	import com.dbi.cat.common.constants.EntryPointType;
	import com.dbi.cat.common.constants.EntryRestrictionType;
	
	import mx.collections.ArrayCollection;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.EntryPointDefinition")]
	public class EntryPointDefinitionVO
	{
		public function EntryPointDefinitionVO()
		{
			super();
		}
		
		public var primaryKey:Number;
		public var description:String;
		public var value:String;
		public var type:String = EntryPointType.SMS;
		public var restriction:String = EntryRestrictionType.SHARED;
		public var clientIDs:ArrayCollection;
		public var restrictionID:Number;
		public var keywords:ArrayCollection;
		
		public function get children():ArrayCollection
		{
			return keywords;
		}
		public function get name():String
		{
			return value;
		}
	}
}