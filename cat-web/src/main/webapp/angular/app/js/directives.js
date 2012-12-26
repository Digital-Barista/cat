'use strict';

/* Directives */


angular.module('cat.directives', [])
    .directive('leftmenu', function (config, $location) {
        return {
            restrict: 'A',
            replace: true,
            templateUrl: 'partials/leftmenu.html',
            link: function(scope, element, attrs){
                scope.path = $location.path();
                scope.menuItems = config.leftNavItems;
                scope.select = function(item){
                    scope.path = item.url;
                }

            }
        }
    })
    .directive('choosenetworks', function(){
        return {
            restrict: 'A',
            replace: true,
            templateUrl: 'partials/choosenetworks.html',
            link: function(scope, element, attrs){
                scope.message = {
                    facebook: 'all',
                    facebookContacts: 0
                }
            }
        }
    })

    .directive('sessiontimeout', function($rootScope, $location){
        return {
            restrict: 'A',
            replace: true,
            templateUrl: 'partials/sessiontimeout.html',
            link: function(scope, element, attrs){
                var sessionTime = 60 * 30 * 1000,
                    warningTime = 60 * 5 * 1000,
                    start = new Date().getTime();

                function countDown(){
                    scope.timeRemaining = Math.round(sessionTime - (new Date().getTime() - start));
                    scope.$apply(function(){
                        $rootScope.showSessionTimeout = scope.timeRemaining < warningTime;
                    });
                    if (scope.timeRemaining <= 0){
                        scope.logout();
                    }
                    setTimeout(countDown, 200);
                }

                scope.logout = function (){
                    start = new Date().getTime();
                    $location.path('login');
                }
                scope.refreshSession = function(){
                    start = new Date().getTime();
                }

                $rootScope.$on('$routeChangeSuccess', function(evt, cur, prev) {
                    scope.refreshSession();
                });

                setTimeout(countDown, 200);
            }
        }
    });
