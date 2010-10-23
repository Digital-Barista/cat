package com.dbi.cat.business
{
	import com.dbi.cat.common.constants.EntryPointType;
	import com.dbi.cat.common.vo.CampaignMessagePartVO;
	import com.dbi.cat.common.vo.NodeVO;
	import com.dbi.cat.view.workspace.MessagePreviewView;
	
	import flash.display.DisplayObject;
	import flash.external.ExternalInterface;
	import flash.net.FileReference;
	
	import mx.collections.ArrayCollection;
	import mx.core.Application;
	import mx.core.IFlexDisplayObject;
	import mx.formatters.DateFormatter;
	import mx.managers.PopUpManager;
	
	[Bindable]
	public class CampaignMessageManager
	{
		public var messageParts:ArrayCollection;
		
		private var messagePreviewWindow:IFlexDisplayObject;
		
		public function CampaignMessageManager()
		{
		}

		public function loadMessagePart(title:String, part:CampaignMessagePartVO):void
		{
			var formatter:DateFormatter = new DateFormatter();
			formatter.formatString = "MMMM D at K:NN A";
			var dateString:String = formatter.format(new Date());
			
			var wholeMessage:String = "";
			if (part.entryType == EntryPointType.TWITTER.name)
			{
				for each (var m:String in part.messages)
				{
					var twitterContent:String = 
						"<table id='MessageTable'>" +
							"<tr>" +
								"<td class='profileCell'></td>" +
								"<td class='messageCell'>" +
									"<div>" + m + "</div>" +
									"<div id='MessageDate'>" + dateString + "</div>" +
								"</td>" +
							"</tr>" +
						"</table>";
					wholeMessage += twitterContent;
				}
			}
			else if (part.entryType == EntryPointType.SMS.name)
			{
				for (var i:Number = 0; i < part.messages.length; i++)
				{
					var message:String = part.messages[i];
					
					if (i > 0)
						wholeMessage += "<br /><br />";
					
					if (part.messages.length > 1)
						wholeMessage += "<b>Message " + (i + 1) + ":</b><br />";
					
					wholeMessage += message;
				}
			}
			
			var escapedBody:String = escape(wholeMessage);
			var escapedTitle:String = escape(title);
			
			var script:String = "function(){var win = window.open('assets/preview/" + part.entryType + "/index.html');" + 
				"win.date = '" + dateString + "';" +
				"win.title = unescape('" +  escapedTitle + "');" +
				"win.message = unescape('" + escapedBody + "');}";
			ExternalInterface.call(script);
		}
		
		public function openMessagePreview():void
		{
			messageParts = null;
			
			if (messagePreviewWindow == null)
			{
				messagePreviewWindow = new MessagePreviewView();
			}
			
			PopUpManager.addPopUp(messagePreviewWindow, DisplayObject(Application.application), true);
			PopUpManager.centerPopUp(messagePreviewWindow);
		}
		
		public function closeMessagePreview():void
		{
			PopUpManager.removePopUp(messagePreviewWindow);
		}
	}
}