define([
  'jquery',
  'underscore',
  'backbone',
  'vm',
	'events',
  'text!templates/layout.html' 
], function($, _, Backbone, Vm, Events, layoutTemplate){
  var AppView = Backbone.View.extend({
    el: '.container',
    initialize: function () {
		

    },
    
    render: function () {
			var that = this;
      $(this.el).html(layoutTemplate);

      // Add main menu
      require([
      'views/menu/mainmenu',
      'models/menu'], 
      
      function (MainMenuView, Menu) {
        var menu = new Menu();
        var mainMenuView = Vm.create(that, 'MainMenuView', MainMenuView, {model: menu});
        mainMenuView.render();
        mainMenuView.selectMenuItem('/');
      });
		} 
	});
  
  return AppView;
});
