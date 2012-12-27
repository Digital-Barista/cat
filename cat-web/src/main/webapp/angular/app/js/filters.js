'use strict';

/* Filters */

angular.module('cat.filters', [])
    .filter('entryPointType', function(){
        return function(input, entryPointType){
            if (!input){
                return [];
            }
            return $.grep(input, function(entry){
               return entry.type == entryPointType;
            });
        }
    })
    .filter('timelength', function(){
        return function(input){
            if (input <= 0)
                return '';

            var time = Math.round(input / 1000),
                ret = '',
                secMinutes = 60,
                secHours = secMinutes * 60,
                secDays = secHours * 24,
                secWeeks = secDays * 7;

            if (time >= secWeeks) {
                var weeks = Math.floor(time / secWeeks);
                ret += weeks + "w ";
                time -= weeks * secWeeks;
            }
            if (time >= secDays) {
                var days = Math.floor(time / secDays);
                ret += days + "d ";
                time -= days * secDays;
            }
            if (time >= secHours) {
                var hours = Math.floor(time / secHours);
                ret += hours + "h ";
                time -= hours * secHours;
            }
            if (time >= secMinutes) {
                var minutes = Math.floor(time / secMinutes);
                ret += minutes + "m ";
                time -= minutes * secMinutes;
            }
            if (time > 0) {
                ret += time + "s";
            }
            return ret;
        }
    });
