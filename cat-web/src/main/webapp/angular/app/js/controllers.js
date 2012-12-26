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
            var emptyMessage = {
                message: ''
            }
            $scope.message = $.extend({}, emptyMessage);

            $scope.resetMessage = function(){
                $scope.showConfirmReset = true;
            }

            $scope.confirmReset = function(){
                $scope.message = $.extend({}, emptyMessage);
                $scope.showConfirmReset = false;
            }

            $scope.chooseNetworks = function(){
                $scope.showNetwork = true;
            }
        }
    ])
    .controller('SendCouponCtrl', [
        '$scope',
        'config',
        function ($scope) {
           var emptyCoupon = {
               infiniteCoupons: true,
               infiniteRedemptions: true,
               maxCoupons: 0,
               maxRedemptions: 0,
               couponCode: 'random',
               expiration: 'never',
               available: 'available',
               message: ''
           }

           $scope.coupon = $.extend({}, emptyCoupon);

           $scope.resetCoupon = function(){
               $scope.showConfirmReset = true;
           }

            $scope.confirmReset = function(){
                $scope.coupon = $.extend({}, emptyCoupon);
                $scope.showConfirmReset = false;
            }

            $scope.sendCoupon = function(){
                $scope.showSend = true;
            }

            $scope.confirmSend = function(){

            }

            $scope.chooseNetworks = function(){
                $scope.showNetwork = true;
            }
        }
    ])
    .controller('SentBroadcastCtrl', [
        '$scope',
        function ($scope) {
        }
    ]);
