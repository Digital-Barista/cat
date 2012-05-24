// Filename: router.js
define([
  'jquery',
  'underscore',
  'backbone',
	'vm'
], function ($, _, Backbone, Vm) {
  var AppRouter = Backbone.Router.extend({
    routes: {
      // Pages
      'message/sendcoupon': 'sendcoupon',	
      'message/sendmessage': 'sendmessage',
      'message/sent': 'sent',
      'message': 'sendcoupon',
    
      // Default - catch all
      '*actions': 'defaultAction'
    }
  });

  var initialize = function(options){
    
		var appView = options.appView;
    var router = new AppRouter(options);
    
		router.on('route:defaultAction', function (actions) {
			require(['views/dashboard/summary'], function (Summary) {
        var summary = Vm.create(appView, 'Summary', Summary);
        summary.render();
      });
		});
    
    router.on('route:sendcoupon', function () {
     require(['views/message/sendcoupon'], function (SendCouponView) {
        var sendCoupon= Vm.create(appView, 'SendCouponView', SendCouponView);
        sendCoupon.render();
      });     
    });
		
		router.on('route:sendmessage', function () {
	   require(['views/message/sendmessage'], function (SendMessageView) {
        var sendMessage= Vm.create(appView, 'SendMessageView', SendMessageView);
        sendMessage.render();
      });	  	
		});
    
    router.on('route:sent', function () {
     require(['views/message/sent'], function (SentView) {
        var sent = Vm.create(appView, 'SentView', SentView);
        sent.render();
      });     
    });
		

    Backbone.history.start({ pushState: true });
  };
  
  return {
    initialize: initialize
  };
});
