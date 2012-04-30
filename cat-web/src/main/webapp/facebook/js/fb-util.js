dbi = this.dbi || {};
(function(){
	dbi.log = function(message){
		if (console && console.log){
			console.log(message);
		}
	};
	
	dbi.setup = function(){
		var form = document.getElementById('FBAppForm');
		if (!form){
			dbi.log('"FBAppForm" is not defined!');
		}
		else{
			var tags = form.getAttribute('data-tags').split('&');
			for (var i = 0; i < tags.length; i++){
				var parts = tags[i].split('=');
				
				var item = document.createElement('div');
				form.appendChild(item);
				
				var check = document.createElement('input');
				check.setAttribute('type', 'checkbox');
				check.setAttribute('value', parts[0]);
				item.appendChild(check);
				item.appendChild(document.createTextNode(parts[1]));
			}
			
			var submitLabel = form.getAttribute('data-submit') || 'Subscribe';
			var submit = document.createElement('input');
			submit.setAttribute('type', 'submit');
			submit.setAttribute('value', submitLabel);
			form.appendChild(submit);
			
			submit.onclick = dbi.send;
		}
	};
	
	dbi.send = function(event){
		event.preventDefault();

		var form = document.getElementById('FBAppForm');
		var app = form.getAttribute('data-fb-app');
		if (!app){
			dbi.log('"data-fb-app" is not defined!');
		}
		else{
			var url = 'http://apps.facebook.com/' + app + '/';
			var inputs = form.getElementsByTagName('input');
			var tags = '';
			for (var i = 0; i < inputs.length; i++){
				if (inputs[i].type == 'checkbox' &&
					inputs[i].checked){
					if (tags.length > 0){
						tags += ",";
					}
					tags += inputs[i].getAttribute('value');
				}
			}
			if (tags.length > 0){
				url += '?tags=' + tags;
			}
			window.location = url;
		}
	};
	
	window.onload = dbi.setup;
})();