package com.dbi.cat.view.workspace
{
	import com.dbi.cat.common.vo.LayoutInfoVO;
	
	public interface ILayoutInfoItem
	{
		function get layoutInfo():LayoutInfoVO;
		function set layoutInfo(layout:LayoutInfoVO):void;
		function get voUID():String;
	}
}