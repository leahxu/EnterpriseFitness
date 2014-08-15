/// <reference path="_references.js" />

/// data needs to be in correct format for highcharts
/// [{
///   name: 'Line name...',
///   data: [ [134234, 0   ], ...

function plotLineChart(container, data, title, ytitle, clickEvent, chartType) {
    if (title == undefined) { title = '' }
    if (ytitle == undefined) { ytitle = '' }
    if (chartType == undefined) { chartType = 'spline' }

    $('#' + container).highcharts({
        chart: {
            type: chartType
        },
        title: {
            text: title
        },
        xAxis: {
            type: 'datetime',
            dateTimeLabelFormats: { // don't display the dummy year
                month: '%e. %b',
                year: '%b'
            }
        },
        yAxis: {
            title: {
                text: ytitle
            },
            min: 0
        },
        credits: {
            enabled: false
        },
        //legend: {
        //    layout: 'vertical',
        //    align: 'right',
        //    verticalAlign: 'middle'
        //},
        tooltip: {
            formatter: function () {
                return '<b>' + this.series.name + '</b><br/>' +
                Highcharts.dateFormat('%e. %b', this.x) + ': ' + this.y;
            }
        },
        plotOptions: {
            series: {
                cursor: 'pointer',
                point: {
                    events: {
                        click: clickEvent
                    }
                }
            }
        },

        series: data
    });
}

function plotPieChart(container, data, title, labelFormat) {
    $('#' + container).highcharts({
        chart: {
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false,
            width: 500,
            height: 400,
            marginTop: 25,
            marginBottom: 1
        },
        title: {
            text: title
        },
        tooltip: {
            pointFormat: '<b>{point.percentage:.1f}% /{point.y}</b>'
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: false,
                    color: '#000000',
                    connectorColor: '#000000',
                    connectorPadding: 0,
                    distance: -40,
                    format: '<br/> {y} <br/> ({point.percentage:.1f}%)'
                },
                showInLegend: true
            }
        },
        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'middle',
            useHTML: true,
            floating: false,
            width: 190,
            labelFormatter: labelFormat || function () { return this.name; }
        },
        credits: {
            enabled: false
        },
        series: [{
            type: 'pie',
            data: data,
            dataLabels: {
                formatter: function () {
                    return this.y;
                }
            }
        }]
    });
}

function plotCharts(container, url, plot, parameters, contentType) {
    $("#" + container).text("loading...");
    if (contentType === undefined) {
        contentType = "json";
    }

    $.get(url, $.param(parameters, false),
        function (data) {
            $("#" + container).text("");
            $("#" + container).parent().children(".resetZoom").show();

            plot(container, data);
        }, contentType)
        .fail(function (data) {
            $("#" + container).text("Error:" + data.statusText);
        });
}

function loadContent(container, url, parameters) {
    plotCharts(container, url,
        function (c, data) { $("#" + container).html(data); },
        parameters, "html");
}

Date.prototype.addDays = function (num) {
    var value = this.valueOf();
    value += 86400000 * num;
    return new Date(value);
}

function plotmchart(container, data) {
}

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}