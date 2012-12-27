'use strict';


angular.module('cat.controllers', [])
    .controller('DashboardCtrl',
        function ($scope) {
        }
    )
    .controller('SendMessageCtrl',
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
    )
    .controller('SendCouponCtrl',
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
    )
    .controller('SentBroadcastCtrl',
        function ($scope) {
        }
    )
    .controller('RedeemCouponsCtrl',
        function ($scope, CouponServices) {
            $scope.redeem = function(){
                delete $scope.message;
                CouponServices.redeemCoupon({
                    code: $scope.couponCode,
                    success: function(result){
                        $scope.message = result.message;
                        $scope.messageClass = result.code == 0 ? 'alert-success' : 'alert-error';
                    }
                })
            }
        }
    )
    .controller('SentCouponsCtrl',
        function ($scope, CouponServices) {
            CouponServices.listCoupons({
                success: function(result){
                    $scope.coupons = result;
                }
            })
        }
     );
