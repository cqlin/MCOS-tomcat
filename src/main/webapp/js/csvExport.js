/**
 * To use this code, add your HTML table with an id attribute (e.g., <table id="my_id_table_to_export">...</table>), and then create a download button or link like this:
 * <a href="#" onclick="download_table_as_csv('my_id_table_to_export');">Download as CSV</a>
 */
// Quick and simple export target #table_id into a CSV
function download_table_as_csv(table_id, separator = ',') {
    // Select rows from table_id
    let rows = document.querySelectorAll('table#' + table_id + ' tr');
    
    // Construct CSV
    let csv = [];
    for (const element of rows) {
        let row = [],
            cols = element.querySelectorAll('td, th');
        for (let j = 0; j < cols.length; j++) {
            // Clean innertext to remove multiple spaces and line breaks (break CSV)
            let data = cols[j].innerText.replace(/(\r\n|\n|\r)/gm, '').replace(/(\s\s)/gm, ' ');
            // Escape double-quote with double-double-quote
            data = data.replace(/\"/g, '""');
            // Push escaped string
            row.push('"' + data + '"');
        }
        csv.push(row.join(separator));
    }
    
    let csv_string = csv.join('\n');
    
    // Download it
    let filename = 'export_' + table_id + '_' + new Date().toLocaleDateString() + '.csv';
    let link = document.createElement('a');
    link.style.display = 'none';
    link.setAttribute('target', '_blank');
    link.setAttribute('href', 'data:text/csv;charset=utf-8,' + encodeURIComponent(csv_string));
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

/*
 * export table using jQuery
 * To use this function, create an HTML table with an id attribute (e.g., <table id="my_id_table_to_export">...</table>), and then add a download button or link like this:
 * <a href="#" onclick="exportTableToCSV($('#my_id_table_to_export'), 'my_exported_table.csv');">Download as CSV</a> 
 */

function exportTableToCSV($table, filename) {
    let $rows = $table.find('tr:has(td), tr:has(th)'),
        tmpColDelim = String.fromCharCode(11), // Temporary delimiter characters unlikely to be typed by keyboard
        tmpRowDelim = String.fromCharCode(0), // Null character
        colDelim = '","',
        rowDelim = '"\r\n"',
        csv = '"' + $rows.map(function(i, row) {
            let $row = jQuery(row),
                $cols = $row.find('td, th');
            return $cols.map(function(j, col) {
                let $col = jQuery(col),
                    text = $col.text();
                return text.replace(/"/g, '""'); // Escape double quotes
            }).get().join(tmpColDelim);
        }).get().join(tmpRowDelim)
        .split(tmpRowDelim).join(rowDelim)
        .split(tmpColDelim).join(colDelim) + '"',
        csvData = 'data:application/csv;charset=utf-8,' + encodeURIComponent(csv);

    if (window.navigator.msSaveBlob) {
        // For IE 10+
        window.navigator.msSaveOrOpenBlob(new Blob([csv], { type: "text/plain;charset=utf-8;" }), "csvname.csv");
    } else {
        // For other browsers
        jQuery(this).attr({
            'download': filename,
            'href': csvData,
            'target': '_blank'
        });
    }
}
