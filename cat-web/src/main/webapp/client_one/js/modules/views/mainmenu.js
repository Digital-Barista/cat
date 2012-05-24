define([
  'jquery',
  'underscore',
  'backbone',
  'text!templates/menu/mainmenu.html' 
], function($, _, Backbone, mainMenuTemplate){
  var MainMenuView = Backbone.View.extend({
  
    el: '.menu',
    template: _.template(mainMenuTemplate),
    
    intialize: function () {
    },
    
    render: function () {
      $(this.el).html(this.template(this.model.toJSON()));
    },
    
    events: {
      'click a': 'menuItemClick'
    },
    
    menuItemClick: function (ev) {
      ev.preventDefault();
      this.selectMenuItem($(ev.target).attr('href'));
    },
    
    selectMenuItem: function(url) {
      this.$el.find('.active').removeClass('active');
      this.$el.find('.sub-menu a[href="' + url + '"]')
          .closest('li')
          .addClass('active')
          .closest('.menu-item')
          .addClass('active');
    }
  })

  return MainMenuView;
});
