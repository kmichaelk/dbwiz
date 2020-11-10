/**
 * Module: Word
 *
 * Callbacks:
 * - _wordReadDocument
 * - _wordReplaceInDocument
 * - _wordSaveDocument
 * - _wordCloseBook
 * 
 * Created on 13/10/20
 * (c) Mikhail K., 2020
 */

function WordDocument(filePath) {
    this.filePath = filePath;
    this.id = _wordReadDocument(filePath);
}
WordDocument.prototype.replace = function(toReplace, replaceWith) {
    _wordReplaceInDocument(this.id, toReplace, replaceWith);
    return this;
}
WordDocument.prototype.save = function(targetPath) {
    if(!targetPath) {
        targetPath = this.filePath;
    }
    _wordSaveDocument(this.id, targetPath);
}
WordDocument.prototype.close = function() {
    this.filePath = null;
    if(this.id != -1) {
        _wordCloseDocument(this.id);
    }
}