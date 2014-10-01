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
      'cat/message/sendcoupon': 'sendcoupon',	
      'cat/message/sendmessage': 'sendmessage',
      'cat/message/sent': 'sent',
      'cat/message': 'sendcoupon',
    
      // Default - catch all
      '*actions': 'defaultAction'
    }
  });

  var initialize = function(options){
    
		var appView = options.appView;
    var router = new AppRouter(options);
    
		router.on('route:defaultAction', function (actions) {
			require(['views/dashboard/summary',
			         'collections/dashboarddatas'], function (Summary, DashboardCollection) {
			  var dashboardData = new DashboardCollection();
        var summary = Vm.create(appView, 'Summary', Summary, {appView: appView, collection: dashboardData});
        dashboardData.deferred.done(function(){
          summary.render();
        });
      });
		});
    
    router.on('route:sendcoupon', function () {
     require(['views/message/sendcoupon',
              'models/coupon'], function (SendCouponView, Coupon) {
        var coupon = new Coupon();
        var sendCoupon= Vm.create(appView, 'SendCouponView', SendCouponView, {appView: appView, model: coupon});
        sendCoupon.render();
      });     
    });
		
		router.on('route:sendmessage', function () {
	   require(['views/message/sendmessage',
              'models/message'], function (SendMessageView, Message){
        var message = new Message();
        var sendMessage= Vm.create(appView, 'SendMessageView', SendMessageView, {appView: appView, model: message});
        sendMessage.render();
      });	  	
		});
    
    router.on('route:sent', function () {
     require(['views/message/sent',
              'collections/broadcasts'], function (SentView, Broadcasts) {
        var broadcasts = new Broadcasts();
        var sent = Vm.create(appView, 'SentView', SentView, {appView: appView, collection: broadcasts});
        broadcasts.deferred.done(function(){
          sent.render();
        });
      });     
    });
		

    Backbone.history.start({ pushState: true });
  };
  
  return {
    initialize: initialize
  };
});
