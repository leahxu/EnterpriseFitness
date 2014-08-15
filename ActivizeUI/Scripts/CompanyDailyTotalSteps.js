/// <reference path="_references.js" />
/// <reference path="push/pushCommonCharting.js" />

$(function () {
    refresh();

    $('.namespaceQuery').typeahead([
   {
       name: 'accounts',
       prefetch: '/Company/getCompanyTotalStepHistory',
       remote: '/Company/getCompanyTotalStepHistory'
   }
    ]).on('typeahead:selected', function () { refresh(); });

    //TODO: Doing this to work around issue with typehead.js
    $('.namespaceQuery').parent().children('input').css('width', '300px');
});

function refresh() {

    $("#companyDailyTotalSteps").html('loading...');
    plotCharts("companyDailyTotalSteps", "/Company/getCompanyTotalStepHistory", plotKPI, {
        metric: $("#metric").val()
    });

}

function plotKPI(container, data) {

    $("#namespaceName").html('<h2>Namespace:' + data['NamespaceName'] + '</h2>');
    $("#scaleUnitName").html('<h2>ScaleUnit:' + data['ScaleUnit'] + '</h2>');
    plotMessagingKPIData(container, data['NamespaceCompanys'], data['NamespaceName']);
}

function plotMessagingKPIData(container, data, namespace) {

    $('#' + container).highcharts({
        chart: {
            type: 'line'
        },
        title: {
            text: namespace
        },
        xAxis: {
            type: 'datetime'
            //categories: dates,
        },
        yAxis: {
            min: 0,
            title: {
                text: ''
            }
        },
        tooltip: {
            headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
            pointFormat: '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' +
                '<td style="padding:0"><b>{point.y:.1f}</b></td></tr>',
            footerFormat: '</table>',
            shared: true,
            useHTML: true
        },
        plotOptions: {
            line: {
                color: '#eb7126',
                borderWidth: 0
            }
        },
        credits: {
            enabled: false
        },

        series: data
    });
}
