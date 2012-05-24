require([
  "namespace",

  // Libs
  "jquery",
  "use!backbone",

  // Modules
  "modules/menu",
  "modules/dashboard",
  "modules/message",
],

function(namespace, $, Backbone, Menu, Dashboard, Message) {

  // Defining the application router, you can attach sub routers here.
  var Router = Backbone.Router.extend({
    routes: {
      "message/sendcoupon": "sendcoupon",
      "message/sendmessage": "sendmessage",
      "message/sent": "sent",
      "message": "sendcoupon",
      "": "index",
      ":hash": "index",
    },

    index: function(hash) {
      var summary = new Dashboard.Views.Summary();
      var mainMenu = new Menu.Views.Main();

      // Add main menu
      mainMenu.render(function(el){
        $("#menu").html(el);
      });
      
      // Attach the tutorial to the DOM
      summary.render(function(el) {
        $("#main").html(el);
      });
      
    },

    showMessageMenu: function(){
      var menu = new Menu.Views.Message();

      menu.render(function(el){
        $("#sub-menu").html(el);
      });
    },
    
    sendcoupon: function(hash) {
      var coupon = new Message.Views.SendCoupon();
      
      this.showMessageMenu();
      coupon.render(function(el){
        $("#main").html(el);
      });

    },

    sendmessage: function(hash) {
      var message = new Message.Views.SendMessage();

      this.showMessageMenu();
      message.render(function(el){
        $("#main").html(el);
      });
    },

    sent: function(hash) {
      var sent = new Message.Views.Sent();

      this.showMessageMenu();
      sent.render(function(el){
        $("#main").html(el);
      });
    }
  });

  // Shorthand the application namespace
  var app = namespace.app;

  // Treat the jQuery ready function as the entry point to the application.
  // Inside this function, kick-off all initialization, everything up to this
  // point should be definitions.
  $(function() {
    // Define your master router on the application namespace and trigger all
    // navigation from this instance.
    app.router = new Router();

    // Trigger the initial route and enable HTML5 History API support
    Backbone.history.start({ pushState: true });
  });

  // All navigation that is relative should be passed through the navigate
  // method, to be processed by the router.  If the link has a data-bypass
  // attribute, bypass the delegation completely.
  $(document).on("click", "a:not([data-bypass])", function(evt) {
    // Get the anchor href and protcol
    var href = $(this).attr("href");
    var protocol = this.protocol + "//";

    // Ensure the protocol is not part of URL, meaning its relative.
    if (href && href.slice(0, protocol.length) !== protocol &&
        href.indexOf("javascript:") !== 0) {
      // Stop the default event to ensure the link will not cause a page
      // refresh.
      evt.preventDefault();

      // `Backbone.history.navigate` is sufficient for all Routers and will
      // trigger the correct events.  The Router's internal `navigate` method
      // calls this anyways.
      Backbone.history.navigate(href, true);
    }
  });

});
