package com.dbi.cat.common.controls
{
	import mx.controls.List;
	import mx.core.ClassFactory;

	public class DisabledList extends List
	{
		public function DisabledList()
		{
			super();
			this.itemRenderer = new ClassFactory(DisabledListItemRenderer);
		}
		
	}
}