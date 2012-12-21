'use strict';


// Declare app level module which depends on filters, and services
angular.module('cat', ['cat.filters', 'cat.services', 'cat.directives', 'cat.controllers', 'ui']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('', {templateUrl: 'partials/dashboard.html', controller: 'DashboardCtrl'});
    $routeProvider.when('/sendmessage', {templateUrl: 'partials/sendmessage.html', controller: 'SendMessageCtrl'});
    $routeProvider.when('/sendcoupon', {templateUrl: 'partials/sendcoupon.html', controller: 'SendCouponCtrl'});
    $routeProvider.when('/sendbroadcast', {templateUrl: 'partials/sendbroadcast.html', controller: 'SendBroadcastCtrl'});
    $routeProvider.otherwise({redirectTo: ''});
  }]);
