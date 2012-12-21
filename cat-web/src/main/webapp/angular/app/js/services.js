'use strict';

angular.module('cat.services', [])
    .value('ui.config', {
        tinymce: {
            theme: 'simple',
        }
    })
    .factory('config', function () {
        return {
            leftNavItems:[
                {
                    title:'Dashboard',
                    type: 'header'
                },
                {
                    title:'Summary',
                    url:''
                },
                {
                    title:'Message',
                    type: 'header'
                },
                {
                    title:'Send Coupons',
                    url:'/sendcoupon'
                },
                {
                    title:'Send Message',
                    url:'/sendmessage'
                },
                {
                    title:'Sent Broadcasts',
                    url:'/sendbroadcast'
                }
            ]
        }
    });
