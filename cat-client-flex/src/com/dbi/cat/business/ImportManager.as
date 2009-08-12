package com.dbi.cat.business
{
	import com.dbi.cat.view.address.ImportView;
	import com.dbi.controls.CustomMessage;
	
	import flash.events.Event;
	import flash.net.FileReference;
	
	import mx.collections.ArrayCollection;
	import mx.core.Application;
	import mx.core.IFlexDisplayObject;
	import mx.core.UIComponent;
	import mx.managers.PopUpManager;
	import mx.utils.StringUtil;
	
	[Bindable]
	public class ImportManager
	{
		private var fileRef:FileReference;
		private var importAddressPopup:IFlexDisplayObject;
		
		public var importedAddresses:ArrayCollection;
		public var allClientAddresses:ArrayCollection;
		
		public function ImportManager()
		{
		}

		public function chooseImportFile():void
		{
			fileRef = new FileReference();
			fileRef.addEventListener(Event.SELECT, selectFile);
			fileRef.browse();
		}
		public function openImportWindow():void
		{
			importedAddresses = new ArrayCollection();
			importAddressPopup = new ImportView();
			importAddressPopup.height = Application.application.height - 20;
			
			PopUpManager.addPopUp(importAddressPopup, UIComponent(Application.application), true);
			PopUpManager.centerPopUp(importAddressPopup);
		}
		public function importSucceded(addresses:Array):void
		{
			CustomMessage.show("Successfully imported " + addresses.length + " addresses");
		}
		public function closeImportWindow():void
		{
			if (importAddressPopup != null)
				PopUpManager.removePopUp(importAddressPopup);
			importAddressPopup = null;
		}
		
		public function loadClientAddresses(list:ArrayCollection):void
		{
			allClientAddresses = new ArrayCollection();
			for each (var s:String in list)
			{
				allClientAddresses.addItem({address:StringUtil.trim(s)});
			}
			importedAddresses = allClientAddresses;
		}
		
		private function selectFile(e:Event):void
		{
			fileRef.load();
			fileRef.addEventListener(Event.COMPLETE, fileUploaded);
		}
		private function fileUploaded(e:Event):void
		{
			var lines:Array = fileRef.data.toString().split("\n");
			for each (var s:String in lines)
			{
				importedAddresses.addItem({address:StringUtil.trim(s)});
			}
		}
	}
}