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
    })

    .directive('contactchart', function(){
        return {
            restrict: 'A',
            replace: true,
            templateUrl: 'partials/dashboardcharts.html',
            link: function(scope, element, attrs){

                google.load('visualization', '1.0', {'packages':['corechart'], 'callback': drawChart});

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
                    var options = {'title':'Contacts',
                        'width':330,
                        'height':300};

                    // Instantiate and draw our chart, passing in some options.
                    var chart = new google.visualization.PieChart(document.getElementById('contacts'));
                    chart.draw(data, options);

                    var data = google.visualization.arrayToDataTable([
                        ['Month', 'Subscribers'],
                        ['Jan',  1000],
                        ['Feb',  1170],
                        ['Mar',  660],
                        ['Apr',  1030]
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
    });
