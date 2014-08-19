'use strict';

angular.module('cat.controllers').controller('SentCouponsCtrl',
    function ($scope, $rootScope, CouponServices) {
        $scope.paging = {
            limit: 50,
            offset: 0,
            total: 0
        }

        $rootScope.$on('pagechange', function (event, pageIndex) {
            $scope.paging.offset = pageIndex * $scope.paging.limit;
            listCoupons();
        });

        function listCoupons() {
            CouponServices.listCoupons({
                data: {
                    offset: $scope.paging.offset,
                    limit: $scope.paging.limit
                },
                success: function (result) {
                    $scope.coupons = result;
                }
            });
        }
        listCoupons();
    }
);