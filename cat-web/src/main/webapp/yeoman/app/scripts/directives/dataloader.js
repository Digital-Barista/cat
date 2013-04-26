'use strict';

angular.module('cat.directives').directive('dataloader', function ($rootScope) {
    return {
        restrict: 'A',
        replace: true,
        templateUrl: 'views/dataloader.html',
        link: function (scope, element, attrs) {
            $rootScope.$on('dataloader', function (event, show) {
                scope.showDataLoader = show ? 'on' : '';
            });
        }
    }
});