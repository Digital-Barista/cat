'use strict';

angular.module('cat.directives').directive('modalloader', function ($rootScope) {
    return {
        restrict: 'A',
        replace: true,
        templateUrl: 'views/modalloader.html',
        link: function (scope, element, attrs) {
            var timeout,
                timeoutMils = 20000;
            $rootScope.$on('modalloader', function (event, show) {
                setVisibility(show);
            });

            function setVisibility(visible) {
                clearTimeout(timeout);
                scope.showModalLoader = visible ? 'on' : '';

                if (visible) {
                    timeout = setTimeout(function () {
                        scope.showModalLoader = '';
                    }, timeoutMils);
                }
            }
        }
    }
});