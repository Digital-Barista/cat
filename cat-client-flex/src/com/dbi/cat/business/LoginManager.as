package com.dbi.cat.business
{
	import com.dbi.cat.common.constants.FaultCode;
	import com.dbi.cat.common.vo.UserVO;
	import com.dbi.cat.event.CampaignEvent;
	import com.dbi.cat.event.CampaignMessageEvent;
	import com.dbi.cat.event.CampaignTestEvent;
	import com.dbi.cat.event.ClientEvent;
	import com.dbi.cat.event.ContactEvent;
	import com.dbi.cat.event.LoadDataEvent;
	import com.dbi.cat.event.NavigationEvent;
	import com.dbi.cat.event.SubscriptionEvent;
	import com.dbi.cat.event.UserEvent;
	import com.dbi.controls.CustomMessage;
	import com.dbi.event.CustomMessageEvent;
	
	import flash.events.TimerEvent;
	import flash.external.ExternalInterface;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	import flash.utils.Timer;
	
	import mx.controls.Alert;
	import mx.core.Application;
	import mx.messaging.ChannelSet;
	import mx.messaging.config.ServerConfig;
	import mx.rpc.AsyncToken;
	import mx.rpc.Responder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	[Bindable]
	public class LoginManager
	{
		public static const TIMEOUT_MINUTES:Number = 60;
		
		public static const STATE_LOGIN:String = null;
		public static const STATE_LOGIN_ATTEMPT:String = "LoginAttempt";
		public static const STATE_LOGIN_FAIL:String = "LoginFail";
		
		private var dispatcher:IEventDispatcher;
		
		public var loginState:String = STATE_LOGIN;
		public var loginUsername:String;
		public var loginPassword:String;
		public var currentUser:UserVO;
		
		private var lastActivity:Date;
		private var activityTimer:Timer;
		
		public function LoginManager(dispatcher:IEventDispatcher)
		{
			this.dispatcher = dispatcher;
			activityTimer = new Timer(5000);
			activityTimer.addEventListener(TimerEvent.TIMER, checkLastActivity);
		}

		/**
		 * Updates the last activity date to check against for session timeout
		 */
		public function updateLastActivity():void
		{
			lastActivity = new Date();
		}
		
		private function checkLastActivity(e:TimerEvent):void
		{
			var now:Date = new Date();
			var diff:Number = now.time - lastActivity.time;
			if (diff > (TIMEOUT_MINUTES * 60 * 1000))
			{
				logout();
				CustomMessage.show("You have been logged out after " + TIMEOUT_MINUTES + " minutes of inactivity",
					null, 
					closeLogoutMessage);
			}
		}
		
		private function closeLogoutMessage(e:CustomMessageEvent):void
		{
			reloadApplication();
		}
		
		/**
		 * Sends a request to authenticate the user
		 * 
		 * @param username Username of the user
		 * @param password Password of the user
		 */
		public function login(username:String, password:String):void
		{
			loginState = STATE_LOGIN_ATTEMPT;
			var cs:ChannelSet = ServerConfig.getChannelSet("userManager");
			var token:AsyncToken = cs.login(username, password);
			token.addResponder(new Responder(loginSuccess, loginFail));
		}

		/**
		 * Ends the session for the user, closes all open popups and
		 * returns the user to the login screen
		 */
		public function logout():void
		{
			// Stop activity timer
			activityTimer.stop();
			
			// Kill the flex session
			try
			{
				var cs:ChannelSet = ServerConfig.getChannelSet("userManager");
				cs.logout();
			}
			catch(e:Error)
			{
			}
			
			// Reset model properties
			currentUser = null;
			
			// Move to login screen
			dispatcher.dispatchEvent(new NavigationEvent(NavigationEvent.LOGIN));
			
			// Close any open popups
			dispatcher.dispatchEvent(new CampaignEvent(CampaignEvent.CLOSE_EDIT_CAMPAIGN));
			dispatcher.dispatchEvent(new CampaignEvent(CampaignEvent.CLOSE_COMMUNICATIONS));
			dispatcher.dispatchEvent(new CampaignTestEvent(CampaignTestEvent.END_TEST_CAMPAIGN));
			dispatcher.dispatchEvent(new ClientEvent(ClientEvent.CANCEL_EDIT));
			dispatcher.dispatchEvent(new UserEvent(UserEvent.CANCEL_EDIT));
			dispatcher.dispatchEvent(new ClientEvent(ClientEvent.CLOSE_EDIT_ENTRY_POINT));
			dispatcher.dispatchEvent(new ClientEvent(ClientEvent.CLOSE_EDIT_KEYWORD));
			dispatcher.dispatchEvent(new ClientEvent(ContactEvent.CLOSE_CONTACT));
			dispatcher.dispatchEvent(new ClientEvent(ContactEvent.CLOSE_ASSIGN_CONTACT_TAG));
			dispatcher.dispatchEvent(new ClientEvent(ContactEvent.CLOSE_UNASSIGN_CONTACT_TAG));
			dispatcher.dispatchEvent(new SubscriptionEvent(SubscriptionEvent.CLOSE_NODE_SUBSCRIBERS));
			dispatcher.dispatchEvent(new CampaignMessageEvent(CampaignMessageEvent.CLOSE_MESSAGE_PREVIEW));
			dispatcher.dispatchEvent(new ClientEvent(ClientEvent.CLOSE_ADD_CREDITS));
		}
		
		/**
		 * Reload the application in the browser to clear everything
		 * out of memory
		 */
		public function reloadApplication():void
		{
			var urlRequest:URLRequest = new URLRequest("javascript:location.reload(true)");
			navigateToURL(urlRequest,"_self");
		}
		
		/**
		 * Loads a user into the app as the current user
		 * 
		 * @param result Result object from the login service call
		 */
		public function loadCurrentUser(result:Object):void
		{
			// Load user
			currentUser = result as UserVO;
			loginState = STATE_LOGIN;
			
			// Clear login fields
			loginUsername = loginUsername == "" ? null : "";
			loginPassword = loginPassword == "" ? null : "";
			
			// Login to app
			dispatcher.dispatchEvent(new NavigationEvent(NavigationEvent.DASHBOARD));
		}
    public function updateCurrentUser(user:UserVO):void
    {
        // If the passed user is the current one update it
        if (user &&
            user.primaryKey == currentUser.primaryKey)
        {
          currentUser = user;   
        }
    }
    
		private function loginSuccess(result:ResultEvent):void
		{
			// Start activity timer
			updateLastActivity();
			activityTimer.start();
			
			// Load all data needed for initialization
			dispatcher.dispatchEvent(new LoadDataEvent(LoadDataEvent.INITIALIZE_DATA));
		}
		private function loginFail(fault:FaultEvent):void
		{
			// If an authentication error is thrown set the error state
			if (fault.fault.faultCode == FaultCode.AUTHENTICATION_FAILED ||
				fault.fault.faultCode == FaultCode.AUTHENTICATION_ERROR)
			{
				loginState = STATE_LOGIN_FAIL;
				logout();
			}
			// If an unexpected fault happens show the result
			else
			{
				loginState = STATE_LOGIN;
				Alert.show(fault.toString());
			}
			
			// Clear password on fail
			loginPassword = loginPassword == "" ? null : "";
		}
	}
}