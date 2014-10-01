define([
  'underscore',
  'backbone'
], function(_, Backbone) {
  var DashboardData = Backbone.Model.extend({
    
    defaults: {
      contactCount: 0,
      messageTotal: 0,
    },
    
    initialize: function(){
      var self = this,
          contacts = this.get('contactCounts'),
          messages = this.get('messagesSent');
      
      // Sum contacts
      _.each(contacts, function(contact){
        self.set('contactCount', self.get('contactCount') + contact.count);
      });

      // Sum sent messages
      _.each(messages, function(message){
        self.set('messageTotal', self.get('messageTotal') + message.count);
      });
    }
  });
  
  return DashboardData;

});
