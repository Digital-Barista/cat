package com.dbi.cat.event
{
	import flash.events.Event;

	public class ImportEvent extends Event
	{
		public static const OPEN_IMPORT_WINDOW:String = "openImportWindowEvent";
		public static const CLOSE_IMPORT_WINDOW:String = "closeImportWindowEvent";
		public static const CHOOSE_IMPORT_FILE:String = "chooseImportFileEvent";
		public static const DO_IMPORT:String = "doImportEvent";
		public static const LOAD_ALL_CLIENT_ADDRESSES:String = "loadAllClientAddressesEvent";
		
		public var addresses:Array;
		public var entryPointUID:String;
		public var clientId:Number;
		public var entryPointType:String;
			
		public function ImportEvent(type:String)
		{
			super(type, true, false);
		}
	}
}