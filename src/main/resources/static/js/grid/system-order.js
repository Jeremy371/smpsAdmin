define(function (require) {
    "use strict";

    var GridBase = require('../dist/jqgrid.js');
    var GetUrl = require('./../getUrl.js');

    var JSON = '.json';
    var XLSX = '.xlsx';

    return GridBase.extend({
        initialize: function (options) {
            this.parent = options.parent;
            this.param = options.param;
            this.baseName = options.baseName;

            var colModel = [
/*                {name: 'admissionNm', label: '전형'},
                {name: 'typeNm', label: '계열'},
                {name: 'examDate', label: '시험일자'},
                {name: 'deptNm', label: '모집단위'},*/
                {name: 'groupNm', label: '조'},
                {name: 'groupOrder', label: '면접순서'},
                {name: 'debateNm', label: '토론 조'},
                {name: 'debateOrder', label: '발언순서'}
            ];

            for (var i = 0; i < colModel.length; i++) {
                colModel[i].label = colModel[i].label === undefined ? colModel[i].name : colModel[i].label;
            }

            var opt = $.extend(true, {
                defaults: {
                    url: new GetUrl({baseName: this.baseName, suffix: JSON, param: this.param}).getUrl(),
                    colModel: colModel
                }
            }, options);

            this.constructor.__super__.initialize.call(this, opt);
        },
        render: function () {
            this.constructor.__super__.render.call(this);
          //  this.addExcel(new GetUrl({baseName: this.baseName, suffix: XLSX, param: this.param}).getUrl());
            return this;
        }
    });
});