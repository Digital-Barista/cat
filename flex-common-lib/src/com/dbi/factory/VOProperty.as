package com.dbi.factory
{
	/**
	 * Holds information about a classes properties that is used by <code>VOFactory</code>
	 * to convert <code>IValueObject</code> objects to and from XML.
	 *  
	 * @author khoyt2
	 * 
	 */	
	public class VOProperty
	{
		public function VOProperty()
		{
		}
		
		public var voName:String;
		public var voClassType:Class;
		public var serviceName:String;
		public var decodeFunctionName:String;
		public var encodeFunctionName:String;
		public var collectionClassTypeName:String;
		public var collectionClassType:Class;
		public var isValueObject:Boolean;
		public var field:String;
		public var ignore:String;

	}
}