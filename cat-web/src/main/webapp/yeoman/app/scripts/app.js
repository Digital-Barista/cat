'use strict';

angular.module('cat', ['cat.services', 'cat.controllers', 'cat.directives', 'cat.filters', 'ui'])
  .config(function ($routeProvider) {
    $routeProvider
        .when('', {templateUrl: 'views/dashboard.html', controller: 'DashboardCtrl'})
        .when('/sendmessage', {templateUrl: 'views/sendmessage.html', controller: 'SendMessageCtrl'})
        .when('/sendcoupon', {templateUrl: 'views/sendcoupon.html', controller: 'SendCouponCtrl'})
        .when('/sentbroadcast', {templateUrl: 'views/sentbroadcast.html', controller: 'SentBroadcastCtrl'})
        .when('/sentcoupons', {templateUrl: 'views/sentcoupons.html', controller: 'SentCouponsCtrl'})
        .when('/redeemcoupons', {templateUrl: 'views/redeemcoupons.html', controller: 'RedeemCouponsCtrl'})
      .otherwise({
        redirectTo: ''
      });
  })
    .value('ui.config', {
        tinymce: {
            theme : "advanced",
            mode: "none",
            plugins : "autoresize,fullscreen,autolink,lists,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,xhtmlxtras,template,advlist",

            width: '100%',
            height: 400,
            autoresize_min_height: 400,
            autoresize_max_height: 800,

            // Theme options
            theme_advanced_buttons1 : "bold,italic,underline,|,justifyleft,justifycenter,justifyright,formatselect,fontselect,fontsizeselect",
            theme_advanced_buttons2 : "cut,copy,paste,pasteword,|,bullist,numlist,|,outdent,indent,|,undo,redo,|,link,unlink,image,help,code,|,forecolor,backcolor,|,media",
            theme_advanced_buttons3 : "",
            theme_advanced_toolbar_location : "top",
            theme_advanced_toolbar_align : "left",
            theme_advanced_statusbar_location : "bottom",
        }
    });
