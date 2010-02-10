package com.dbi.controls
{
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.containers.HBox;
	import mx.containers.VBox;
	import mx.controls.Label;
	import mx.controls.LinkButton;
	import mx.events.IndexChangedEvent;

	[Event(name="pageIndexChanged", type="mx.events.IndexChangedEvent")]
		
	public class PagingControl extends VBox
	{
		
		public static const DEFAULT_PAGE_SIZE:Number = 100;
		public static const MAX_PAGE_NUMBER_BUTTONS:Number = 10;
		
		public var navButtonStyleName:Object;
		public var selectedNavButtonStyleName:Object;
		
		private var _totalResults:Number = 0;
		/**
		 * Total number of results to page through
		 */
		public function get totalResults():Number
		{
			return _totalResults;
		}
		public function set totalResults(value:Number):void
		{
			if (_totalResults != value)
			{
				_totalResults = value;
				updateControls();
			}
		}
		
		private var _pageIndex:Number = 0;
		/**
		 * Current index of the page of results that should be shown
		 */
		public function get pageIndex():Number
		{
			return _pageIndex;
		}
		public function set pageIndex(value:Number):void
		{
			if (_pageIndex != value)
			{
				_pageIndex = value;
				updateControls();
			}
		}
		
		private var _pageSize:Number = DEFAULT_PAGE_SIZE;
		/**
		 * Number of results to show at once
		 */
		public function get pageSize():Number
		{
			return _pageSize;
		}
		public function set pageSize(value:Number):void
		{
			if (_pageSize != value)
			{
				_pageSize = value;
				updateControls();
			}
		}
		
		private var _maxPageNumberButtons:Number = MAX_PAGE_NUMBER_BUTTONS;
		/**
		 * Number of results to show at once
		 */
		public function get maxPageNumberButtons():Number
		{
			return _maxPageNumberButtons;
		}
		public function set maxPageNumberButtons(value:Number):void
		{
			if (_maxPageNumberButtons != value)
			{
				_maxPageNumberButtons = value;
				updateControls();
			}
		}
		
		// Calculated properties
		private var numPages:Number = 0;
		private var startPageIndex:Number = 0;
		private var endPageIndex:Number = 0;
		
		// Navigation controls
		private var resultLabel:Label;
		private var controlContainer:HBox;
		private var firstNav:LinkButton;
		private var previousNav:LinkButton;
		private var nextNav:LinkButton;
		private var lastNav:LinkButton;
		private var navButtonContainer:HBox;
		
		public function PagingControl()
		{
			super();
			setStyle("horizontalAlign", "center");
			setStyle("verticalGap", 0);
			
			resultLabel = new Label();
			addChild(resultLabel);
			
			controlContainer = new HBox();
			controlContainer.setStyle("verticalAlign", "middle");
			controlContainer.setStyle("horizontalAlign", "center");
			addChild(controlContainer);
			
			firstNav = new LinkButton();
			firstNav.label = "First";
			firstNav.addEventListener(MouseEvent.CLICK, changeIndex);
			controlContainer.addChild(firstNav);
			
			previousNav = new LinkButton();
			previousNav.label = "Prev";
			previousNav.addEventListener(MouseEvent.CLICK, changeIndex);
			controlContainer.addChild(previousNav);
			
			navButtonContainer = new HBox();
			navButtonContainer.setStyle("verticalAlign", "middle");
			navButtonContainer.setStyle("horizontalAlign", "center");
			controlContainer.addChild(navButtonContainer);
			
			nextNav = new LinkButton();
			nextNav.label = "Next";
			nextNav.addEventListener(MouseEvent.CLICK, changeIndex);
			controlContainer.addChild(nextNav);
			
			lastNav = new LinkButton();
			lastNav.label = "Last";
			lastNav.addEventListener(MouseEvent.CLICK, changeIndex);
			controlContainer.addChild(lastNav);
		}
		
		private function updateControls():void
		{
			// Recalculate page properties
			if (_pageSize <= 0)
				_pageSize = DEFAULT_PAGE_SIZE;
						
			numPages = Math.ceil(totalResults / pageSize);
			
			if (_pageIndex >= numPages)
				_pageIndex = numPages - 1;
				
			if (_pageIndex <= 0)
				_pageIndex = 0;
				
			if (_maxPageNumberButtons <= 0)
				_maxPageNumberButtons = MAX_PAGE_NUMBER_BUTTONS;
				
			startPageIndex = 0;
			endPageIndex = startPageIndex + maxPageNumberButtons;
			
			if (numPages > maxPageNumberButtons)
			{
				var half:Number = Math.floor(maxPageNumberButtons / 2);
				startPageIndex = pageIndex - half;
					  
				
				if (startPageIndex < 0)
					startPageIndex = 0;
					
				endPageIndex = startPageIndex + maxPageNumberButtons;
				
				if (endPageIndex > numPages)
				{
					while (startPageIndex > 0 &&
						numPages - startPageIndex < maxPageNumberButtons)
						startPageIndex--;
				}
			}
			
			// Set results label text
			if (totalResults <= 0)
			{
				resultLabel.text = "Showing 0 items";
			}
			else
			{
				var firstResult:Number = pageIndex * pageSize;
				var lastResult:Number = firstResult + pageSize;
				if (lastResult > totalResults)
					lastResult = totalResults;
					
				resultLabel.text = "Showing " + (firstResult + 1) + " - " + lastResult + " of " + totalResults + " items";
			}
			
			
			// Update navigation page indexes
			firstNav.enabled = pageIndex > 0;
			firstNav.data = 0;
			
			previousNav.enabled = pageIndex > 0;
			previousNav.data = pageIndex - 1;
			
			navButtonContainer.enabled = numPages > 1;
			
			nextNav.enabled = pageIndex < numPages - 1;
			nextNav.data = pageIndex + 1;
			
			lastNav.enabled = pageIndex < numPages - 1;
			lastNav.data = numPages - 1;
			
			navButtonContainer.removeAllChildren();
			for (var i:Number = startPageIndex; i < endPageIndex && i < numPages; i++)
			{
				var button:LinkButton = new LinkButton();
				button.label = (i + 1).toString();
				button.data = i;
				button.addEventListener(MouseEvent.CLICK, changeIndex);
				
				// Set appropriate style
				if (pageIndex == i)
				{
					if (selectedNavButtonStyleName != null)
						button.styleName = selectedNavButtonStyleName;
				}	
				else if (navButtonStyleName != null)
				{
					button.styleName = navButtonStyleName;
				}
				
				// Add the button to the container
				navButtonContainer.addChild(button);
			}
		}
		
		private function changeIndex(e:MouseEvent):void
		{
			var event:IndexChangedEvent = new IndexChangedEvent("pageIndexChanged", true, false, this, pageIndex, e.currentTarget.data);
			
			pageIndex = e.currentTarget.data;
			updateControls();
			
			dispatchEvent(event);
		}
	}
}