package com.dbi.cat.business
{
    import com.dbi.cat.common.vo.CouponNodeVO;
    import com.dbi.cat.common.vo.MessageVO;
    import com.dbi.cat.view.broadcast.ConfirmBroadcastView;
    import com.dbi.cat.view.workspace.WorkspaceView;
    
    import flash.display.DisplayObject;
    
    import mx.collections.ArrayCollection;
    import mx.core.Application;
    import mx.core.IFlexDisplayObject;
    import mx.managers.PopUpManager;
	
	
	[Bindable]
	public class BroadcastMessageManager
	{
		
		private var confirmBroadcastPopup:ConfirmBroadcastView;
    
    public var broadcastMessages:ArrayCollection;
    
		public function BroadcastMessageManager()
		{
		}

    public function broadcastMessageResult(result:Object):void
    {
    }
    
    public function broadcastCouponMessageResult(result:Object):void
    {
    }
    
    public function loadBroadcastMessages(result:ArrayCollection):void
    {
        broadcastMessages = result;
    }
    
    public function openConfirmBroadcast(message:MessageVO, coupon:CouponNodeVO):void
    {
        if (confirmBroadcastPopup == null)
        {
          confirmBroadcastPopup = new ConfirmBroadcastView();
        }
        
        confirmBroadcastPopup.message = message;
        confirmBroadcastPopup.coupon = coupon;
                    
        PopUpManager.addPopUp(confirmBroadcastPopup, DisplayObject(Application.application), true);
        PopUpManager.centerPopUp(confirmBroadcastPopup);
    }
    
    public function closeConfirmBroadcast():void
    {
        PopUpManager.removePopUp(confirmBroadcastPopup);
    }
	}
}