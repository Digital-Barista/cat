package com.dbi.cat.event
{
	import com.dbi.cat.common.vo.NodeVO;
	
	import flash.events.Event;

	public class ImportEvent extends Event
	{
		public static const DO_IMPORT:String = "doImportEvent";
		public static const OPEN_IMPORT_SUBSCRIBERS:String = "openImportSubscribers";
		public static const CLOSE_IMPORT_SUBSCRIBERS:String = "closeImportSubscribers";
		
		public var contacts:Array;
		public var node:NodeVO;
			
		public function ImportEvent(type:String)
		{
			super(type, true, false);
		}
	}
}