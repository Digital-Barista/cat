'use strict';

angular.module('cat.directives').directive('sessiontimeout', function ($rootScope, $location) {
    return {
        restrict: 'A',
        replace: true,
        templateUrl: 'views/sessiontimeout.html',
        link: function (scope, element, attrs) {
            var sessionTime = 60 * 30 * 1000,
                warningTime = 60 * 5 * 1000,
                start = new Date().getTime();

            function countDown() {
                scope.timeRemaining = Math.round(sessionTime - (new Date().getTime() - start));
                scope.$apply(function () {
                    $rootScope.showSessionTimeout = scope.timeRemaining < warningTime;
                });
                if (scope.timeRemaining <= 0) {
                    scope.logout();
                }
                setTimeout(countDown, 200);
            }

            scope.logout = function () {
                start = new Date().getTime();
                window.location = contextPath + '/login';
            }
            scope.refreshSession = function () {
                start = new Date().getTime();
            }

            $rootScope.$on('$routeChangeSuccess', function (evt, cur, prev) {
                scope.refreshSession();
            });

            setTimeout(countDown, 200);
        }
    }
});