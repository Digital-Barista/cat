define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/dashboard/summary.html'
], function($, _, Backbone, summaryTemplate){
  var SummaryView = Backbone.View.extend({
    el: '.page',
    render: function () {
		
      $(this.el).html(summaryTemplate);
    }
  });
  return SummaryView;
});
