'use strict';

angular.module('cat.controllers').controller('SendCouponCtrl',
    function ($scope, $rootScope) {
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

        $scope.chooseNetworks = function(){
            $scope.showNetwork = true;
        }

        $rootScope.$on('confirmChooseNetwork', function(e, message){
            $scope.contacts = message.facebook == 'all' ? 'all' : message.facebookContacts;
            $scope.showSendCoupon = true;
        });
    }
);