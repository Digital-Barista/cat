package com.dbi.controls
{
	import mx.controls.ComboBox;

	/**
	 * ComboBox with additional methods to aid in data binding.
	 * <p>
	 * The AdvancedComboBox allows you to set a <code>selectedBindingProperty</code> that will be looked for
	 * on the dataprovider and if that value matches the given <code>selectedBindingValue</code>
	 * the <code>selectedIndex</code> will be set to that item.
	 * </p>
	 * <p>
	 * If the <code>selectedBindingProperty</code> and <code>selectedBindingValue</code> are both set
	 * but the specified value is not matched the <code>bindingBehavior</code> will determine which
	 * item will be selected.  The default "empty" means that if the value cannot be found the selectedIndex
	 * will be set to -1 leaving the combox with an empty selection.  If "none" is specified the selectedIndex
	 * will not be changed from it's current value.  If "first" is specified the first item in the dataProvider
	 * will be selected.
	 * </p>
	 *  
	 * @author khoyt2
	 * 
	 */
	public class AdvancedComboBox extends ComboBox
	{
		public static const BINDING_BEHAVIOR_NONE:String = "none";
		public static const BINDING_BEHAVIOR_EMPTY:String = "empty";
		public static const BINDING_BEHAVIOR_FIRST:String = "first";
		
		
		public var _selectedBindingProperty:String;
		public var _selectedBindingValue:Object;
		private var _bindingBehavior:String = BINDING_BEHAVIOR_EMPTY;
		
		public function get bindingBehavior():String
		{
			return _bindingBehavior;
		}
		public function set bindingBehavior(value:String):void
		{
			_bindingBehavior = value;
		}	
		public function get selectedBindingProperty():String
		{
			return _selectedBindingProperty;
		}
		public function set selectedBindingProperty(value:String):void
		{
			_selectedBindingProperty = value;
			setupSelected();
		}
		public function get selectedBindingValue():Object
		{
			return _selectedBindingValue;
		}
		public function set selectedBindingValue(value:Object):void
		{
			_selectedBindingValue = value;
			setupSelected();
		}
		
		public function AdvancedComboBox()
		{
			super();
		}
		
		/**
		 * Setup the selected item when the dataProvider changes
		 */
		override public function set dataProvider(value:Object):void
		{
			super.dataProvider = value;
			setupSelected();
		}
		
		/**
		 * Search through dataProvider for a value to select if
		 * the selectedBindingValue and selectedBindingProperty
		 * are set
		 */		
		public function setupSelected():void
		{
			if (_bindingBehavior == BINDING_BEHAVIOR_EMPTY)
				selectedIndex = -1;
			else if (_bindingBehavior == BINDING_BEHAVIOR_FIRST &&
				dataProvider != null &&
				dataProvider.length > 0)
				selectedIndex = 0;
				
			if (selectedBindingProperty != null &&
				selectedBindingValue != null &&
				dataProvider != null)
			{
				for (var i:int = 0; i < dataProvider.length; i++)
				{
					if (dataProvider[i].hasOwnProperty(selectedBindingProperty) &&
						dataProvider[i][selectedBindingProperty] == selectedBindingValue)
					{
						selectedIndex = i;
						break;
					}
				}
			}
		}
		
	}
}