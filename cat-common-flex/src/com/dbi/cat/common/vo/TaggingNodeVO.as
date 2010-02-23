package com.dbi.cat.common.vo
{
	import mx.collections.ArrayCollection;
	
	
	[Bindable]
	[RemoteClass(alias="com.digitalbarista.cat.business.TaggingNode")]
	public class TaggingNodeVO extends NodeVO
	{
		public function TaggingNodeVO()
		{
			super();
		}
		
		public var tags:ArrayCollection = new ArrayCollection();
		
		override public function get valid():Boolean
		{
			return tags != null && tags.length > 0;
		}
	}
}