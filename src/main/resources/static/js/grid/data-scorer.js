define(function (require) {
    "use strict";
    var GridBase = require('../dist/jqgrid.js');

    return GridBase.extend({
        initialize: function (options) {
            var colModel = [];

            $.ajax({
                url : 'data/scorer.colmodel',
                async : false,
                success : function(data){
                    colModel = data;
                }
            });

            for (var i = 0; i < colModel.length; i++) {
                if(colModel[i].name == 'isPhoto'){
                    colModel[i]['formatter'] = 'select';
                    colModel[i]['editoptions'] =  {value: {true: 'Y', false: 'N'}};
                }
            }

            var opt = $.extend(true, {
                defaults: {
                    url: 'data/scorer.json',
                    colModel: colModel
                }
            }, options);

            this.constructor.__super__.initialize.call(this, opt);
        },
        render: function () {
            this.constructor.__super__.render.call(this);
            this.addExcel('data/scorer.xlsx');
            return this;
        }
    });
});