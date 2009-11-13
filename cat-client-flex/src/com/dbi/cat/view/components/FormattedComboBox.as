package com.dbi.cat.view.components
{
	import com.dbi.controls.AdvancedComboBox;
	
	import flash.events.MouseEvent;

	/**
	 * Displays a label instead of a combo box if there is only
	 * one option
	 */
	public class FormattedComboBox extends AdvancedComboBox
	{
	
		override public function setupSelected():void
		{
			if (dataProvider != null &&
				dataProvider.length == 1)
			{
				selectedIndex = 0;
				enabled = false;
			}
			else
			{
				enabled = true;
				super.setupSelected();
			}
		}	
	}
}