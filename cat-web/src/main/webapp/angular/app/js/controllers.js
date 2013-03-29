'use strict';


angular.module('cat.controllers', [])
    .controller('DashboardCtrl',
        function ($scope) {
        }
    )
    .controller('SendMessageCtrl',
        function ($scope, $rootScope, CampaignServices, ServiceUtil) {
            var emptyMessage = {
                message: '',
                entryPoints: []
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

            $scope.sendMessage = function(){
                $scope.showSendMessage = false;
                CampaignServices.broadcastMessage({
                    data: {
                        clientId: $scope.message.clientId,
                        message: $scope.message.message,
                        entryPoints: $scope.message.entryPoints
                    },
                    success: function(data){
                        if (!ServiceUtil.handleResult(data)){
                            console.log(data);
                        }
                    }
                });
            }

            $rootScope.$on('confirmChooseNetwork', function(e, message){
                $scope.contacts = message.facebook == 'all' ? 'all' : message.facebookContacts;
                $scope.showSendMessage = true;
            });
        }
    )
    .controller('SendCouponCtrl',
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
