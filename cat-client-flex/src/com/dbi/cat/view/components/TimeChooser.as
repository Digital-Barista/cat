package com.dbi.cat.view.components
{
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;
	import mx.containers.HBox;
	import mx.controls.ComboBox;
	import mx.controls.Label;
	import mx.events.ListEvent;

	[Event(name="timeChange", type="flash.events.Event")]
	
	public class TimeChooser extends HBox
	{
		// Controls
		private var hour:ComboBox;
		private var minute:ComboBox;
		private var timeLabel:Label;
		
		// Time data
		private var hours:ArrayCollection;
		private var minutes:ArrayCollection;
		
		// Public values
		public function get selectedTime():Date
		{
			return new Date(0, 0, 0, new Number(hour.selectedItem), new Number(minute.selectedItem));			
		}
		public function set selectedTime(value:Date):void
		{
			if (value != null)
			{ 
				hour.selectedIndex = value.hours;
				minute.selectedIndex = Math.floor(value.minutes / 5);
			}
		}
		
		public function TimeChooser()
		{
			super();
			
			// Create time data
			setupData();
			
			// Create controls
			hour = new ComboBox();
			hour.dataProvider = hours;
			hour.addEventListener(ListEvent.CHANGE, function(e:ListEvent):void{dispatchEvent(new Event("timeChange"))});
			addChild(hour);
			
			timeLabel = new Label();
			timeLabel.width = 5;
			timeLabel.text = ":";
			addChild(timeLabel);
			
			minute = new ComboBox();
			minute.dataProvider = minutes;
			minute.addEventListener(ListEvent.CHANGE, function(e:ListEvent):void{dispatchEvent(new Event("timeChange"))});
			addChild(minute);
		}
		
		private function setupData():void
		{
			// Create hours data
			hours = new ArrayCollection;
			for (var i:Number = 0; i < 24; i++)
			{
				if (i.toString().length == 1)
					hours.addItem("0" + i.toString());
				else
					hours.addItem(i.toString());
			}
			
			// Create minutes data
			minutes = new ArrayCollection();
			for (i = 0; i < 60; i+=5)
			{
				if (i.toString().length == 1)
					minutes.addItem("0" + i.toString());
				else
					minutes.addItem(i.toString());
			}
		}
	}
}