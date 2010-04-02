var channel_path = '/xd_receiver.htm'; 


FB_RequireFeatures(["Api"], function(){ 
	// Create an ApiClient object, passing app's API key and 
	// a site relative URL to xd_receiver.htm 
	FB.Facebook.init(api_key, channel_path); 

	 
	var api = FB.Facebook.apiClient; 

	FB.Connect.requireSession(function(){
		FB.CanvasClient.startTimerToSizeToContent();
		var uid = api.get_session().uid;
		var messageApi = new MessageAPI();
		messageApi.loadMessages(uid);
	},null, true);
	
	
	
	// require user to login 
//	api.requireLogin(function(exception){ 
//
//		if (exception)
//		{
//			alert(exception);
//		}
//		else
//		{
//			FB.CanvasClient.startTimerToSizeToContent();
//			var uid = api.get_session().uid;
//			 var messageApi = new MessageAPI();
//			 messageApi.loadMessages(uid);
//		}
//		 
//	}); 
});  



//FB.ensureInit(function () {
//	FB.Facebook.apiClient.fql_query("SELECT uid, name FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1 = 100000679573930)",
//            function(rows) {
//				var s = "";
//              for (i in rows)
//              {
//            	  s += rows[i].uid + ", " + rows[i].name + "\n";
//              }
//              alert(s);
//            });
//
//  });


