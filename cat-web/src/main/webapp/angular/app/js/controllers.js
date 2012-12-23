'use strict';


angular.module('cat.controllers', [])
    .controller('DashboardCtrl', [
        '$scope',
        function ($scope) {
        }
    ])
    .controller('SendMessageCtrl', [
        '$scope',
        function ($scope) {
        }
    ])
    .controller('SendCouponCtrl', [
        '$scope',
        'config',
        function ($scope) {
           $scope.coupon = {
               infiniteCoupons: true,
               infiniteRedemptions: true,
               maxCoupons: 0,
               maxRedemptions: 0,
               couponCode: 'random'
           }
        }
    ])
    .controller('SendBroadcastCtrl', [
        '$scope',
        function ($scope) {
        }
    ]);
