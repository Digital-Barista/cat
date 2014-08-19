'use strict';

angular.module('cat.controllers').controller('RedeemCouponsCtrl',
    function ($scope, CouponServices) {
        $scope.redeem = function(){
            delete $scope.message;
            CouponServices.redeemCoupon({
                code: $scope.couponCode,
                success: function(response){
                    $scope.message = response.result.message;
                    $scope.messageClass = response.result.code == 0 ? 'alert-success' : 'alert-error';
                }
            })
        }
    }
)