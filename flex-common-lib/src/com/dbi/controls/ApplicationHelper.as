package com.dbi.controls
{
	import flash.display.NativeWindowDisplayState;
	import flash.events.NativeWindowBoundsEvent;
	import flash.events.NativeWindowDisplayStateEvent;
	import flash.net.SharedObject;
	import flash.system.Capabilities;
	
	import mx.core.Application;
	import mx.events.AIREvent;
	

	public class ApplicationHelper
	{
		public static const DEFAULT_X:Number = 0;
		public static const DEFAULT_Y:Number = 0;
		
		private var _lso:SharedObject;
		private function get settings():Object
		{
			if (_lso == null)
			{
				var key:String = Application.application.toString() + "Settings";
				_lso = SharedObject.getLocal(key);
			}
			return _lso.data;
		}
		
		private var app:Object = Application.application;
		
		public function ApplicationHelper()
		{
			super();

			if (Capabilities.playerType == "Desktop")
			{
				// Hide app to prevent flicker
				app.visible = false;
				
				// Load any saved settings when the window has loaded
				app.addEventListener(AIREvent.WINDOW_COMPLETE, setupWindow);
			}
		}
		
		private function changeDisplayState(e:NativeWindowDisplayStateEvent):void
		{
			if (e.afterDisplayState != NativeWindowDisplayState.MINIMIZED)
				settings.displayState = e.afterDisplayState;
		}
		private function changeWindow(e:NativeWindowBoundsEvent):void
		{
			if (settings.displayState == null ||
				settings.displayState == NativeWindowDisplayState.NORMAL)
			{
				settings.xPos = e.afterBounds.x;
				settings.yPos = e.afterBounds.y;
				
				settings.width = e.afterBounds.width;
				settings.height = e.afterBounds.height;
			}
		}
		private function setupWindow(e:AIREvent):void
		{
			// Position window
			if (settings.xPos != null &&
				settings.yPos != null)
			{
				app.nativeWindow.x = settings.xPos;
				app.nativeWindow.y = settings.yPos;
			}
			
			// Size window
			if (settings.width != null &&
				settings.height != null)
			{
				app.nativeWindow.width = settings.width;
				app.nativeWindow.height = settings.height;
			}
			
			// Ensure the window is on the screen
			if (app.nativeWindow.x < 0)
				app.nativeWindow.x = DEFAULT_X;
			if (app.nativeWindow.y < 0 ||
				app.nativeWindow.y > Capabilities.screenResolutionY)
				app.nativeWindow.y = DEFAULT_Y;
			
			// Load displayState of window
			if (settings.displayState != null)
			{
				if (settings.displayState == NativeWindowDisplayState.MAXIMIZED)
				{
					app.nativeWindow.maximize();
				}
			}
			
			// Show app after adjusting window properties
			app.visible = true;
			
			// Listen for changes to window
			app.nativeWindow.addEventListener(NativeWindowDisplayStateEvent.DISPLAY_STATE_CHANGING, changeDisplayState);
			app.nativeWindow.addEventListener(NativeWindowBoundsEvent.RESIZE, changeWindow);
			app.nativeWindow.addEventListener(NativeWindowBoundsEvent.MOVE, changeWindow);
		}
		
	}
}