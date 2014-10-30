'use strict';

angular.module('cat.controllers').controller('SendMessageCtrl',
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