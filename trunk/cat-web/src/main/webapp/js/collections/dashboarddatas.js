define([
  'jquery',
  'underscore',
  'backbone',
  'models/dashboarddata'
], function($, _, Backbone, Dashboard){
  
  var DashboardCollection = Backbone.Collection.extend({
    model: Dashboard,
    url: '/cat/rs/reporting/dashboard',
    
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
        throw new Error("Dashboard fetch did get collection from API");
    }

  });

  return DashboardCollection;
});
