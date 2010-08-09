package com.dbi.cat.admin.business
{
	import com.dbi.cat.admin.view.account.EditFacebookAppView;
	import com.dbi.cat.common.vo.FacebookAppVO;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IViewCursor;
	import mx.core.Application;
	import mx.core.IFlexDisplayObject;
	import mx.core.UIComponent;
	import mx.managers.PopUpManager;
	import mx.utils.ObjectUtil;
	
	[Bindable]
	public class AccountManager
	{
		public var facebookApps:ArrayCollection;
		public var currentFacebookApp:FacebookAppVO;
		
		private var editFacebookAppPopup:IFlexDisplayObject;
		
		public function AccountManager()
		{
		}

		
		//
		// FacebookApp methods
		//
		public function loadFacebookApps(list:ArrayCollection):void
		{
			facebookApps = list;
		}
		
		public function editFacebookApp(facebookApp:FacebookAppVO):void
		{
			currentFacebookApp = ObjectUtil.copy(facebookApp) as FacebookAppVO;
			
			if (editFacebookAppPopup == null)
				editFacebookAppPopup = new EditFacebookAppView();
			PopUpManager.addPopUp(editFacebookAppPopup, UIComponent(Application.application), true);
			PopUpManager.centerPopUp(editFacebookAppPopup);
		}
		public function closeEditFacebookApp():void
		{
			PopUpManager.removePopUp(editFacebookAppPopup);
		}
		public function deleteFacebookApp(facebookApp:FacebookAppVO):void
		{
			var cur:IViewCursor = facebookApps.createCursor();
			while (cur.current != null)
			{
				if (cur.current.facebookAppId == facebookApp.facebookAppId)
					cur.remove();
				else
					cur.moveNext();
			}
		}
		public function saveFacebookApp(facebookApp:FacebookAppVO):void
		{
			var found:Boolean = false;
			for each (var f:FacebookAppVO in facebookApps)
			{
				if (f.facebookAppId == facebookApp.facebookAppId)
				{
					f.apiKey = facebookApp.apiKey;
					f.clientId = facebookApp.clientId;
					f.clientName = facebookApp.clientName;
					f.id = facebookApp.id;
					f.secret = facebookApp.secret;
					found = true;
					break;
				}
			}
			if (!found)
			{
				facebookApps.addItem(facebookApp);
			}
			closeEditFacebookApp();
		}
		
	}
}