// Require.js allows us to configure shortcut alias
// Their usage will become more apparent futher along in the tutorial.
require.config({
  paths: {
    // Major libraries
    jquery: '/cat/js/libs/jquery/jquery-min',
    jqueryui: '/cat/js/libs/jquery/jquery-ui-min',
    tinymce: '/cat/js/libs/tiny_mce/tiny_mce',
    underscore: '/cat/js/libs/underscore/underscore-min', // https://github.com/amdjs
    backbone: '/cat/js/libs/backbone/backbone-min', // https://github.com/amdjs
    sinon: '/cat/js/libs/sinon/sinon.js',

    // Require.js plugins
    text: '/cat/js/libs/require/text',
    order: '/cat/js/libs/require/order',

    // Just a short cut so we can put our html outside the js dir
    // When you have HTML/CSS designers this aids in keeping them out of the js directory
    templates: '/cat/templates',
    
    views: '/cat/js/views',
    models: '/cat/js/models'
  },
	//urlArgs: "bust=" +  (new Date()).getTime()

});

// Let's kick off the application

require([
  'views/app',
  'router',
  'vm'
], function(AppView, Router, Vm){
  var appView = Vm.create({}, 'AppView', AppView);
  appView.render();
  Router.initialize({appView: appView});  // The router now has a copy of all main appview
  

  // All navigation that is relative should be passed through the navigate
  // method, to be processed by the router.  If the link has a data-bypass
  // attribute, bypass the delegation completely.
  $(document).on("click", "a[data-navigate='true']", function(evt) {
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
