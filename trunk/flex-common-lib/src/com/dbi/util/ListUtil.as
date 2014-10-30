package com.dbi.util
{
	import mx.collections.ArrayCollection;

	public class ListUtil
	{
		public function ListUtil()
		{
		}
		
		/**
		 * Take an ArrayCollection  and return a new ArrayCollection
		 * containing only the values of each object in the list
		 * for the property specified.
		 * 
		 * @param list ArrayCollection of objects to look through
		 * @param property Property to access on each list item
		 * 
		 * @return A new ArrayCollection
		 */
		public static function getIdList(list:ArrayCollection, property:String):ArrayCollection
		{
			var ret:ArrayCollection;
			
			if (list != null)
			{
				ret = new ArrayCollection();
				for each (var o:Object in list)
				{
					if (o.hasOwnProperty(property))
						ret.addItem(o[property]);
				}
			}
			return ret;
		}
	}
}