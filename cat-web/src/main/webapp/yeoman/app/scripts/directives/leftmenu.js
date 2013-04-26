'use strict';

angular.module('cat.directives', []).directive('leftmenu', function (config, $location, $rootScope) {
    return {
        restrict: 'A',
        replace: true,
        templateUrl: 'views/leftmenu.html',
        link: function (scope, element, attrs) {
            scope.path = $location.path();
            scope.menuItems = config.leftNavItems;
            scope.select = function (item) {
                scope.path = item.url;
            }

            $rootScope.$on('$routeChangeSuccess', function (evt, cur, prev) {
                scope.path = $location.$$path;
            });
        }
    }
});