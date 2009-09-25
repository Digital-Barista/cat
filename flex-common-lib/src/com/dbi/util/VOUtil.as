package com.dbi.util
{
	import mx.collections.ArrayCollection;
	
	/**
	 * Utility for performing common actions on objects fitting the IValueObject interface
	 * @author khoyt2
	 * 
	 */	
	public class VOUtil
	{
		public function VOUtil()
		{
		}

		/**
		 * XML decoded from a service will decode arrays of objects with single items in
		 * them as an ObjectProxy instead of a collection.  This method converts those cases
		 * to an ArrayCollection with a single item.
		 * 
		 * @param o Decode object from a service call that should be an ArrayCollection
		 * @return An ArrayCollection
		 * 
		 */		
		public static function getCollection(o:Object):ArrayCollection
		{
			var ret:ArrayCollection;
			if (o is ArrayCollection)
			{
				ret = o as ArrayCollection;
			}
			else
			{
				ret = new ArrayCollection();
				ret.addItem(o);
			}
			return ret;
		}
	}
}