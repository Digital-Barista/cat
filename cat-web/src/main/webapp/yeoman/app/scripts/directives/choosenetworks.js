'use strict';

angular.module('cat.directives').directive('choosenetworks', function ($rootScope, ClientServices) {
    return {
        restrict: 'A',
        replace: true,
        templateUrl: 'views/choosenetworks.html',
        link: function (scope, element, attrs) {
            $.extend(scope.message, {
                facebook: 'all',
                facebookContacts: 0,
                entryPoints: []
            });

            scope.$watch('selectedClient', function (newVal) {
                scope.message.clientId = newVal ? newVal.clientId : undefined;
            });

            scope.confirmSend = function () {
                var i, point, points = [scope.message.facebookEntryPoint];
                scope.message.entryPoints = [];
                for (i = points.length; i--;) {
                    point = points[i];
                    if (point) {
                        scope.message.entryPoints.push({
                                entryType: point.type,
                                entryPoint: point.value
                            }
                        );
                    }
                }
                scope.showNetwork = false;
                $rootScope.$broadcast('confirmChooseNetwork', scope.message);
            }


            ClientServices.listClients({
                success: function (data) {
                    scope.clients = data.result;
                    scope.selectedClient = scope.clients ? scope.clients[0] : undefined;
                }
            });
        }
    }
});