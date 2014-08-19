'use strict';

/* Directives */


angular.module('cat.directives', [])
    .directive('dataloader', function ($rootScope) {
        return {
            restrict: 'A',
            replace: true,
            templateUrl: 'partials/dataloader.html',
            link: function (scope, element, attrs) {
                $rootScope.$on('dataloader', function (event, show) {
                    scope.showDataLoader = show ? 'on' : '';
                });
            }
        }
    })
    .directive('modalloader', function ($rootScope) {
        return {
            restrict: 'A',
            replace: true,
            templateUrl: 'partials/modalloader.html',
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
    })
    .directive('modalmessage', function ($rootScope) {
        return {
            restrict: 'A',
            replace: true,
            templateUrl: 'partials/modalmessage.html',
            link: function (scope, element, attrs) {
                $rootScope.$on('showmodal', function (event, message) {
                    scope.message = message;
                    if (!scope.message.buttons) {
                        scope.message.buttons = [
                            {text: 'OK'}
                        ];
                    }
                    scope.showModalMessage = 'on';
                });

                scope.clickButton = function (index) {
                    var buttons = scope.message.buttons;
                    if (buttons && buttons[index] && buttons[index].click) {
                        buttons[index].click();
                    }
                    scope.showModalMessage = '';
                }
            }
        }
    })
    .directive('leftmenu', function (config, $location, $rootScope) {
        return {
            restrict: 'A',
            replace: true,
            templateUrl: 'partials/leftmenu.html',
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
    })
    .directive('choosenetworks', function ($rootScope, ClientServices) {
        return {
            restrict: 'A',
            replace: true,
            templateUrl: 'partials/choosenetworks.html',
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
    })

    .
    directive('sessiontimeout', function ($rootScope, $location) {
        return {
            restrict: 'A',
            replace: true,
            templateUrl: 'partials/sessiontimeout.html',
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
    })

    .directive('contactchart', function () {
        return {
            restrict: 'A',
            replace: true,
            templateUrl: 'partials/dashboardcharts.html',
            link: function (scope, element, attrs) {

                google.load('visualization', '1.0', {'packages': ['corechart'], 'callback': drawChart});

                function drawChart() {

                    // Create the data table.
                    var data = new google.visualization.DataTable();
                    data.addColumn('string', 'Network');
                    data.addColumn('number', 'Number Contacts');
                    data.addRows([
                        ['SMS', 3],
                        ['Email', 1],
                        ['Twitter', 1],
                        ['Facebook', 1]
                    ]);

                    // Set chart options
                    var options = {'title': 'Contacts',
                        'width': 330,
                        'height': 300};

                    // Instantiate and draw our chart, passing in some options.
                    var chart = new google.visualization.PieChart(document.getElementById('contacts'));
                    chart.draw(data, options);

                    var data = google.visualization.arrayToDataTable([
                        ['Month', 'Subscribers'],
                        ['Jan', 1000],
                        ['Feb', 1170],
                        ['Mar', 660],
                        ['Apr', 1030]
                    ]);

                    var options = $.extend(options, {
                        title: 'Subscribers',
                        legend: 'none'
                    });

                    var chart = new google.visualization.LineChart(document.getElementById('subscribers'));
                    chart.draw(data, options);
                }
            }
        }
    })
    .directive('pager', function ($rootScope) {
        return {
            restrict: 'A',
            replace: true,
            templateUrl: 'partials/pager.html',
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
    })