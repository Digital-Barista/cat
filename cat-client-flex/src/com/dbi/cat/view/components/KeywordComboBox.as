package com.dbi.cat.view.components
{
	import com.dbi.controls.AdvancedComboBox;
	
	import mx.core.ClassFactory;

	public class KeywordComboBox extends AdvancedComboBox
	{
		public function KeywordComboBox()
		{
			super();              
			
			this.dropdownFactory = new ClassFactory(KeywordList);
            this.itemRenderer = new ClassFactory(KeywordListItemRenderer);
		}
		
	}
}