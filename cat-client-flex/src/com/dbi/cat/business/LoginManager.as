package com.dbi.cat.business
{
	import com.dbi.cat.common.constants.FaultCode;
	import com.dbi.cat.common.vo.UserVO;
	import com.dbi.cat.event.CampaignEvent;
	import com.dbi.cat.event.CampaignTestEvent;
	import com.dbi.cat.event.ClientEvent;
	import com.dbi.cat.event.LoadDataEvent;
	import com.dbi.cat.event.NavigationEvent;
	import com.dbi.cat.event.UserEvent;
	
	import mx.controls.Alert;
	import mx.messaging.ChannelSet;
	import mx.messaging.config.ServerConfig;
	import mx.rpc.AsyncToken;
	import mx.rpc.Responder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	[Bindable]
	public class LoginManager
	{
		public static const STATE_LOGIN:String = null;
		public static const STATE_LOGIN_ATTEMPT:String = "LoginAttempt";
		public static const STATE_LOGIN_FAIL:String = "LoginFail";
		
		private var dispatcher:IEventDispatcher;
		
		public var loginState:String = STATE_LOGIN;
		public var loginUsername:String;
		public var loginPassword:String;
		public var currentUser:UserVO;
		
		public function LoginManager(dispatcher:IEventDispatcher)
		{
			this.dispatcher = dispatcher;
		}

		public function login(username:String, password:String):void
		{
			loginState = STATE_LOGIN_ATTEMPT;
			var cs:ChannelSet = ServerConfig.getChannelSet("userManager");
			var token:AsyncToken = cs.login(username, password);
			token.addResponder(new Responder(loginSuccess, loginFail));
		}
		private static var test:Boolean = true;
		public function logout():void
		{
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
		}
		
		public function loadCurrentUser(result:Object):void
		{
			// Load user
			currentUser = result as UserVO;
			loginState = STATE_LOGIN;
			
			// Clear login fields
			loginUsername = loginUsername == "" ? null : "";
			loginPassword = loginPassword == "" ? null : "";
			
			// Login to app
			dispatcher.dispatchEvent(new NavigationEvent(NavigationEvent.CAMPAIGNS));
		}
		private function loginSuccess(result:ResultEvent):void
		{
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