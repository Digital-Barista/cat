dbi = this.dbi || {};

$.extend(dbi, {
  
  channel_path: '/cat/facebook/xd_receiver.htm',
  currentUID: undefined,

  fbInit: function() {
  	var appId = document.getElementById('appId').value;
  	
  	// Initialize facebook API
  	FB.init({appId: appId, status: true, cookie: true, xfbml: true});
  
  	// Set canvas size
  	FB.Canvas.setSize();
  
  	// Listen for session change events
  	FB.Event.subscribe('auth.sessionChange', function(response) {
  	  dbi.handleResponse(response);
  	});
  	
  	// Check the login status
  	FB.getLoginStatus(function(response) {
  	  dbi.handleResponse(response);
  	});
  	
  	// Wire events
		$("#InviteButton").click(function(){
        FB.ui({method: 'apprequests',
          message: 'Thank you for accessing our app!'
        });
		});
		
		$("#LoginButton").click(function(e){
		  e.preventDefault();
		  FB.login(function(response){
		    dbi.handleResponse(response);
		  });
		});
		
  },
  
  handleResponse: function(response){
  	if (response.authResponse ||
  		response.session) 
  	{
  	  // Clear all app requests
  	  dbi.deleteAllAppRequests();
  	  
  	  // Get UID from response
  		if (response.session){
  		  dbi.currentUID = response.session.uid;
  		}
  		else{
  		  dbi.currentUID = response.authResponse.userID;
  		  dbi.signedRequest = response.authResponse.signedRequest;
  		}
  		
  		dbi.hideLogin();
  		dbi.fetchMessages();
  		dbi.showBookmark();
		  
		  FB.Canvas.setSize();
	  } 
	  else 
	  {
	    dbi.showLogin();
	    dbi.hideBookmark();
	  }
  },
  
  showLogin: function(){
  	$("#LoadingMessage").css("display", "none");
  	$("#Login").css("display", "block");
  	$("#MessageArea").css("display", "none");
  },
  
  hideLogin: function(){
  	$("#Login").css("display", "none");
  	$("#MessageArea").css("display", "block");
  },
  
  showBookmark: function(){
  	$("#Bookmark").css("display", "block");
  },
  
  hideBookmark: function(){
  	$("#Bookmark").css("display", "none");
  },
  
  fetchMessages: function(){
  	var messageApi = new MessageAPI();
  	messageApi.loadMessages();
  },

  fetchQueryParameter: function(name){
  	var query = window.location.search;
  	
  	var parts = query.split("&");
  	for (var i = 0; i < parts.length; i++)
  	{
  		var param = parts[i].split("=");
  		if (param.length == 2 &&
  			param[0] == name)
  		{
  			return param[1];
  		}
  	}
  	return null;
  },
  
  getAppRequests: function(callback){
    FB.api('/me/apprequests', function(result){
      if (callback){
        callback(result);
      }
    });
  },
  
  deleteAppRequest: function(id, callback){
    FB.api('/' + id, 'DELETE', function(result){
      if (callback){
        callback(result);
      }
    });
  },
  
  deleteAllAppRequests: function(){
    dbi.getAppRequests(function(result){
      var i, L,
          data;
      if (result && result.data){
        data = result.data;
        for (i = 0, L = data.length; i < L; i++){
          dbi.deleteAppRequest(data[i].id);
        }
      }
    });
  }
  
});

$(document).ready(function(){
  dbi.fbInit();
});

