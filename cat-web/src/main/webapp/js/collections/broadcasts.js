define([
  'jquery',
  'underscore',
  'backbone',
  'models/broadcast'
], function($, _, Backbone, BroadCast){
  
  var BroadcastCollection = Backbone.Collection.extend({
    model: BroadCast,
    url: '/cat/rs/campaigns/broadcast',
    
    initialize: function () {
        this.fetch({
            success: this.fetchSuccess,
            error: this.fetchError,
            data: {clientID: 1},
            processData: true
        });
        this.deferred = new $.Deferred();
    },
    
    deferred: Function.constructor.prototype,
    
    fetchSuccess: function (collection, response) {
        collection.deferred.resolve();
    },
    
    fetchError: function (collection, response) {
        throw new Error("Broadcasts fetch did get collection from API");
    }

  });

  return BroadcastCollection;
});
