'use strict';

angular.module('cat.services', [])
    .factory('ServiceUtil', function($http, $rootScope){
        return {
            request: function(params){
                var request,
                    defaults = {
                        showLoader: false,
                        showDataLoader: true,
                        cache: false,
                        headers: {'Content-Type': 'application/json'}
                    },
                    config = $.extend(defaults, {showDataLoader: !params.showLoader}, params);

                if (!config.cache){
                    config.params = $.extend({}, config.params, {t: new Date().getTime()});
                }

                if (config.showDataLoader){
                    $rootScope.$broadcast('dataloader', true);
                }

                request = $http(config);
                request.then(function(){
                    if (config.showDataLoader){
                        $rootScope.$broadcast('dataloader', false);
                    }
                });
                return request;
            }
        }
    })
    .factory('ClientServices', function(ServiceUtil){
        var clientUrl = contextPath + '/data/client',
            clientList;

        return {
            listClients: function(params){
                var args = $.extend({}, params);

                if (!clientList || args.reload){
                    ServiceUtil.request({
                        method: 'GET',
                        url: clientUrl,
                        data: args.data
                    }).success(success);
                }
                else {
                    success(clientList);
                }

                function success(result){
                    if (args.success){
                        clientList = result;
                        args.success(clientList);
                    }
                }
            }
        }
    })
    .factory('CouponServices', function(ServiceUtil){
        var couponUrl = contextPath + '/rs/coupons';

        return {

            redeemCoupon: function(params){
                var args = $.extend({}, params),
                    redeemUrl = couponUrl + '/' + args.code;

                ServiceUtil.request({
                    method: 'GET',
                    url: redeemUrl,
                    data: args.data
                }).success(args.success);
            },

            listCoupons: function(params){
                var args = $.extend({}, params);

                ServiceUtil.request({
                    method: 'GET',
                    url: couponUrl,
                    data: args.data
                }).success(args.success);
            }
        }
    })
    .factory('CampaignServices', function(ServiceUtil){
        var broadcastUrl = contextPath + '/data/broadcast';

        return {

            broadcastMessage: function(params){
                var args = $.extend({}, params),
                    url = broadcastUrl;

                ServiceUtil.request({
                    method: 'POST',
                    url: url,
                    data: JSON.stringify(args.data)
                }).success(args.success);
            }
        }
    })
    .value('config', {
            leftNavItems:[
                {
                    title:'Dashboard',
                    type: 'header'
                },
                {
                    title:'Summary',
                    url:''
                },
                {
                    title:'Message',
                    type: 'header'
                },
                {
                    title:'Send Coupons',
                    url:'/sendcoupon'
                },
                {
                    title:'Send Message',
                    url:'/sendmessage'
                },
                {
                    title:'Sent Broadcasts',
                    url:'/sentbroadcast'
                },
                {
                    title: 'Coupons',
                    type: 'header'
                },
                {
                    title: 'Redeem Coupon',
                    url: '/redeemcoupons'
                },
                {
                    title: 'Sent Coupons',
                    url: '/sentcoupons'
                }
            ]
        }
    );
