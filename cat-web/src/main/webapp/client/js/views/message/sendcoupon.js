define([
  "backbone",
  "views/message/messageeditor",
  'text!templates/message/sendcoupon.html'
],

function(Backbone, MessageEditor, sendCouponTemplate) {

  var SendCoupon = Backbone.View.extend({
    el: '.page',
    render: function () {
      $(this.el).html(sendCouponTemplate);
      
      var editor = new MessageEditor();
      editor.render();
    }
  });

  return SendCoupon;

});
