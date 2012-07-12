define([
  'jquery',
  'underscore',
  'backbone',
  'hbs!templates/dashboard/summary'
], function($, _, Backbone, summaryTemplate){
  var SummaryView = Backbone.View.extend({
    el: '.page',
    render: function () {

      var dashboardData = this.collection.at(0).toJSON();
      $(this.el).html(summaryTemplate(dashboardData));
    }
  });
  return SummaryView;
});
