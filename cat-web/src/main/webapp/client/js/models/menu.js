define([
  'underscore',
  'backbone'
], function(_, Backbone) {
  var menuModel = Backbone.Model.extend({
    defaults: {
      menuItems: [
        {title: 'Dashboard',
         url: '/',
         menuItems: [
          {title: 'Summary',
           url: '/'}]
        },
           
        {title: 'Message',
         url: '/message/sendcoupon',
         menuItems: [
          {title: 'Send Coupons',
           url: '/message/sendcoupon'},
           
          {title: 'Send Message',
           url: '/message/sendmessage'},
           
          {title: 'Sent Broadcasts',
           url: '/message/sent'}]
        }
      ]
    },
    
    initialize: function(){
    }

  });
  return menuModel;

});
