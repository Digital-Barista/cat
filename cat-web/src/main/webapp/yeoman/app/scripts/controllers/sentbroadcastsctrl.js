'use strict';

angular.module('cat.controllers').controller('SentBroadcastCtrl',
    function ($scope, $rootScope, CampaignServices, ServiceUtil) {
        $scope.paging = {
            limit: 25,
            offset: 0,
            total: 0
        }

        $rootScope.$on('pagechange', function(event, pageIndex){
            $scope.paging.offset = pageIndex * $scope.paging.limit;
            listBroadcasts();
        });

        function listBroadcasts(){
            CampaignServices.listBroadcasts({
                data: {
                    offset: $scope.paging.offset,
                    limit: $scope.paging.limit
                },
                success: function(data){
                    if (!ServiceUtil.handleResult(data)){
                        $scope.broadcastResult = data;
                        $.extend($scope.paging, data.metadata);
                    }
                }
            });
        }
        listBroadcasts();
    }
)