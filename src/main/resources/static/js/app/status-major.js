define(function (require) {
    "use strict";
    var Backbone = require('backbone');
    var LoadPage = require('../loadPage.js');

    // 초기에 그릴 때 필요, loadPage.js의 같은 부분은 필터 시 다시 그릴 때 require 된다
    require('../grid/status-major.js');
    require('../grid/status-summary.js');
    require('../toolbar/status-major.js');
    require('text!/tpl/status-major.html');
    require('text!/tpl/status-summary.html');

    return Backbone.View.extend({
        initialize: function () {
            new LoadPage({baseName: location.hash.substring(1, location.hash.length), param: window.param}).render();
        }
    });
});