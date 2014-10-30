'use strict';

angular.module('cat.directives').directive('pager', function ($rootScope) {
    return {
        restrict: 'A',
        replace: true,
        templateUrl: 'views/pager.html',
        scope: {
            paging: '='
        },
        link: function (scope, element, attrs) {
            scope.$watch('paging', function(){
                setupPager();
            }, true);

            scope.changePage = function(index){
                if (index != scope.paging.current && index >= 0 && index < scope.paging.count){
                    $rootScope.$broadcast('pagechange', index);
                }
            }

            function setupPager(){
                var i,
                    paging = scope.paging = $.extend({total: 0, limit: 25, offset: 0, maxLinks: 10}, scope.paging),
                    count = scope.paging.count = Math.ceil(paging.total / paging.limit),
                    current = scope.paging.current = Math.floor(paging.offset / paging.limit),
                    mid = Math.floor(Math.min(count, paging.maxLinks)/2),
                    start = count > paging.maxLinks && current > mid ? current - mid : 0,
                    end = Math.min(start + paging.maxLinks, count);

                scope.paging.lastRecord = Math.min(scope.paging.offset + scope.paging.limit + 1, scope.paging.total);
                scope.pages = [];
                for (i = start; i < end; i++){
                    scope.pages.push({index: i, selected: i == current, label: i+1});
                }
                scope.pages.push({index: current+1, label: '>', disabled: current >= end});
                scope.pages.splice(0, 0, {index: current-1, label: '<',  disabled: current <= 0});
            }
        }
    }
});