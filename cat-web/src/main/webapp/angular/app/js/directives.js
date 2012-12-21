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
    });
