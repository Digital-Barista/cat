package com.dbi.cat.common.util
{
	import flash.display.DisplayObject;
	
	import mx.containers.TabNavigator;
	import mx.controls.Button;

	public class SecurityUtil
	{
		public function SecurityUtil()
		{
		}
		
		public static function showTabs(navigator:TabNavigator, ids:Array, show:Boolean):void
		{
			var selectedIndex:int = -1;
			for each (var child:Object in navigator.getChildren())
			{
				var index:int = navigator.getChildIndex(child as DisplayObject);
				
				if (ids.indexOf(child.id) > -1)
				{
					var tab:Button = navigator.getTabAt(index);
					tab.visible = tab.includeInLayout = show;
					
					if (selectedIndex == -1 &&
						show)
					{
						selectedIndex = index;
					}
				}
				else if (selectedIndex == -1)
				{
					selectedIndex = index;
				}
			}
			navigator.selectedIndex = selectedIndex;
		}
	}
}