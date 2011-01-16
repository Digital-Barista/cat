package com.dbi.cat.view.components
{
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.geom.Point;
	
	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.containers.VBox;
	import mx.controls.Button;
	import mx.controls.CheckBox;
	import mx.controls.HRule;
	import mx.controls.RadioButton;
	import mx.controls.RadioButtonGroup;
	import mx.core.Application;
	
	[Event(name="selectedItemsChanged", type="flash.events.Event")]
	public class MultiSelectDropDown extends Button
	{
		
		private var _popup:VBox;
		private var all:RadioButton;
		private var select:RadioButton;
		private var radioGroup:RadioButtonGroup;
		private var optionBox:VBox;
		
		private var _selectedDataItems:ArrayCollection;
		public function get selectedDataItems():ArrayCollection
		{
			return _selectedDataItems;
		}
		public function set selectedDataItems(value:ArrayCollection):void
		{
			_selectedDataItems = value;
		}
		
		private var _dataProvider:ArrayCollection;
		public function get dataProvider():ArrayCollection
		{
			return _dataProvider;
		}
		public function set dataProvider(value:ArrayCollection):void
		{
			_dataProvider = value;
			updateButtonLabel();
		}
		
		private var _dataField:String;
		public function get dataField():String
		{
			return _dataField;
		}
		public function set dataField(value:String):void
		{
			_dataField = value;
		}
		
		public function MultiSelectDropDown()
		{
			super();
			updateButtonLabel();
			addEventListener(MouseEvent.CLICK, onClick);
		}
		
		protected function updateButtonLabel():void
		{
			label = "Select";
			if (_dataProvider != null)
			{
				var selected:String = _selectedDataItems != null ? _selectedDataItems.length.toString() : _dataProvider.length.toString();
				label += " (" + selected + "/" + _dataProvider.length + ")";
			}
		}
		
		public function toggleMenu():void
		{
			if (_popup != null &&
				Application.application.contains(_popup))
			{
				closeMenu();
			}
			else
			{
				openMenu();
			}
		}
		
		protected function onClick(e:MouseEvent):void
		{
			toggleMenu();
		}
		
		public function openMenu():void
		{
			if (_popup == null)
			{
				_popup = buildPopup();
			}
			
			var global:Point = localToGlobal(new Point(0, height));
			_popup.x = global.x;
			_popup.y = global.y;
			
			Application.application.addChild(_popup);
		}
		public function closeMenu():void
		{
			Application.application.removeChild(_popup);
		}
		
		private function buildPopup():VBox
		{
			var box:VBox = new VBox();
			box.setStyle("backgroundColor", 0xFFFFFF);
			box.setStyle("borderStyle", "solid");
			box.setStyle("paddingTop", 10);
			box.setStyle("paddingBottom", 10);
			box.setStyle("paddingLeft", 10);
			box.setStyle("paddingRight", 10);
			
			// Add chnage radio buttons
			radioGroup = new RadioButtonGroup();
			radioGroup.addEventListener(Event.CHANGE, onSelectChange);
			
			all = new RadioButton();
			all.label = "All";
			all.group = radioGroup;
			all.selected = true;
			box.addChild(all);
			
			select = new RadioButton();
			select.label = "Select";
			select.group = radioGroup;
			box.addChild(select);
			
			// Add separator
			var hrule:HRule = new HRule();
			hrule.percentWidth = 100;
			box.addChild(hrule);
			
			// Add select options
			optionBox = new VBox;
			BindingUtils.bindProperty(optionBox, "enabled", select, "selected");
			for each (var item:Object in _dataProvider)
			{
				var check:CheckBox = new CheckBox();
				check.label = item.hasOwnProperty(_dataField) ? item[_dataField] : item.toString();
				check.data = item;
				check.selected = _selectedDataItems != null && _selectedDataItems.contains(item);
				optionBox.addChild(check);
			}
			box.addChild(optionBox);
			
			// Add apply button
			var apply:Button = new Button();
			apply.label = "Apply";
			apply.percentWidth = 100;
			apply.addEventListener(MouseEvent.CLICK, onApply);
			box.addChild(apply);
			
			return box;
		}
		
		protected function onApply(e:MouseEvent):void
		{
			// Add selected items to selected data provider
			if (radioGroup.selection == all)
			{
				_selectedDataItems = null;
			}
			else if (radioGroup.selection == select)
			{
				_selectedDataItems = new ArrayCollection();
				for each (var check:CheckBox in optionBox.getChildren())
				{
					if (check.selected)
					{
						_selectedDataItems.addItem(check.data);
					}
				}
			}
			
			// Update text on the button
			updateButtonLabel();
			
			// Close the popup
			closeMenu();
			
			// Dispatch change event
			dispatchEvent(new Event("selectedItemsChanged", true));
		}
		
		protected function onSelectChange(e:Event):void
		{
		}
	}
}