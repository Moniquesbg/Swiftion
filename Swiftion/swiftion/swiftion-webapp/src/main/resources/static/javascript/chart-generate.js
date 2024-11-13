/**
 * generate all data that is required for a chart
 * @param chartType : type of the chart (bar, line etc)
 * @param jsonArrayJObjects : array of objects that is withdrawn from the thymeleaf template
 * @returns {{data: {datasets: [{backgroundColor: [], data: [], label: string}], labels: []}, options: {responsive: boolean}}}
 */
function generateChartData(chartType, jsonArrayJObjects) {
    dataLabels = [];
    dataSet = [];
    backgroundColors = [];
    chartConfig = chartSetup();

    jsonArrayJObjects.forEach(function(jsonObject){
        dataLabels.push(`${jsonObject.cost_center_name} - ${jsonObject.transaction_type_name}`);
        dataSet.push(parseFloat(jsonObject.swift_copy_amount.replace(/,/g, '')));
        if (jsonObject.transaction_type_name.toLowerCase() === "credit".toLowerCase()) {
            backgroundColors.push('rgba(0, 128, 0, 0.4)');
        } else {
            backgroundColors.push('rgba(128, 0, 0, 0.4)');
        }
    });

    chartConfig.type = chartType;
    chartConfig.data.labels = dataLabels;
    chartConfig.data.datasets[0].data = dataSet;
    chartConfig.data.datasets[0].backgroundColor = backgroundColors;

    return chartConfig;
}

/**
 * setup a basic chart template, fill it with methods where possible
 * @returns {{data: {datasets: [{backgroundColor: *[], data: *[], label: string}], labels: *[]}, options: {responsive: boolean}}}
 */
function chartSetup(){
    return data = {
        data: {
            labels: [],
            datasets: [{
                label: 'Kostenplaatsen',
                data: [],
                backgroundColor: []
            }]
        },
        options: {
            responsive: true
        }
    };
}