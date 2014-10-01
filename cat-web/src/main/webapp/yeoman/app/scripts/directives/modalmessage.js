'use strict';

angular.module('cat.directives').directive('modalmessage', function ($rootScope) {
    return {
        restrict: 'A',
        replace: true,
        templateUrl: 'views/modalmessage.html',
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
});