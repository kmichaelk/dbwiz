/**
 * Module: Excel
 * 
 * Functions:
 * - _excelComputeIndexOfColumn
 * Callbacks:
 * - _excelReadBook
 * - _excelObtainId
 * - _excelWriteCell
 * - _excelSaveBook
 * - _excelCloseBook
 * 
 * Created on 11/10/20
 * (c) Mikhail K., 2020
 */

function ExcelBook(filePath, sheetsToLoad, writeable) {
    this.filePath = filePath;
    this.id = -1;
    if(!sheetsToLoad) {
        sheetsToLoad = [];
    } else {
        if(!Array.isArray(sheetsToLoad)) {
            sheetsToLoad = [ sheetsToLoad ];
        }
    }
    if(writeable) {
        this.id = _excelObtainId();
    }
    var data = _excelReadBook(filePath, this.id, sheetsToLoad);
    if(data === -2) {
        throw new Error('Указанные листы книги не найдены или книга пуста');
    }
    this.sheets = {};
    for(var sheetName in data) {
        var sheet = new ExcelSheet(sheetName, data[sheetName]);
        sheet.book = this;
        this.sheets[sheetName] = sheet;
    }
}
ExcelBook.prototype.promptSheet = function() {
    return this.sheets[openSelectPrompt('Выберите лист Excel', Object.keys(this.sheets))];
}
ExcelBook.prototype.assertWriteability = function() {
    if(this.id == -1) {
        throw new Error('Книга открыта только для чтения');
    }
}
ExcelBook.prototype.save = function(targetPath) {
    this.assertWriteability();
    if(!targetPath) {
        targetPath = this.filePath;
    }
    _excelSaveBook(this.id, targetPath);
}
ExcelBook.prototype.close = function() {
    this.filePath = null;
    this.sheets = null;
    if(this.id != -1) {
        _excelCloseBook(this.id);
    }
}

function ExcelSheet(name, rows) {
    this.name = name;
    this.rows = rows;
}
ExcelSheet.prototype.iterateRows = function(minRow, maxRow, callback) {
    // callback = fun(row, num)
    for(var rowNum = minRow-1; rowNum < maxRow-1; rowNum++) {
        callback(this.rows[rowNum], rowNum);
    }
}
ExcelSheet.prototype.getCell = function(id) {
    var point = _excelParseCellId(id);
    return this.rows[point[0]][point[1]];
}
ExcelSheet.prototype.writeCell = function(row, column, value) {
    this.book.assertWriteability();
    this.rows[row][column] = value;
    _excelWriteCell(this.book.id, this.name, row, column, value);
}
ExcelSheet.prototype.writeCellById = function(cellId, value) {
    var point = _excelParseCellId(cellId);
    this.writeCell(point[0], point[1], value);
}

 // Перевод из двадцатишестиричной системы счисления в десятичную (например, ABA -> 729)
function _excelComputeIndexOfColumn(column) {
    var index = 0;
    var chars = column.split('');
    var len = chars.length;
    for(var i = 0; i < len; i++) {
        var pos = parseInt(chars[i], 26)-9;
        index += pos * Math.pow(26, len-(i+1));
    }
    return index-1;
}

function _excelParseCellId(id) {
    var index = id.indexOf(id.match(/\d/));
    var column = id.substring(0, index);
    var columnIndex = _excelComputeIndexOfColumn(column);
    var row = id.substring(index)-1;
    return [ row, columnIndex ];
}