define([
  'underscore',
  'backbone'
], function(_, Backbone) {
  var menuModel = Backbone.Model.extend({
    defaults: {
      menuItems: [
        {title: 'Dashboard',
         url: '/cat',
         menuItems: [
          {title: 'Summary',
           url: '/cat'}]
        },
           
        {title: 'Message',
         url: '/cat/message/sendcoupon',
         menuItems: [
          {title: 'Send Coupons',
           url: '/cat/message/sendcoupon'},
           
          {title: 'Send Message',
           url: '/cat/message/sendmessage'},
           
          {title: 'Sent Broadcasts',
           url: '/cat/message/sent'}]
        }
      ]
    },
    
    initialize: function(){
    }

  });
  return menuModel;

});
