/**
 * Module: Standard Library
 * 
 * Модуль предоставляет
 * утилиты общего назначения
 * 
 * Created on 19/10/20
 * (c) Mikhail K., 2020
 */

var LINE_BREAK = '\n';

function sizeof(object) {
    return Object.keys(object).length;
}

function merge(target, src) {
    for (var key in src) {
        target[key] = src[key];
    }
}

String.prototype.contains = function (str) {
    return this.includes(str);
}

String.prototype.repl = function (toReplace, replaceWith) {
    return this.valueOf().replace(new RegExp(toReplace, 'g'), replaceWith);
}

String.prototype.removeExtraSpaces = function () {
    return this.valueOf().replace(/ +/g, ' ').trim();
}

String.prototype.format = function () {
    var str = this.valueOf();
    for (var i = 0; i < arguments.length; i++) {
        str = str.replace('%s', arguments[i]);
    }
    return str;
}

Number.prototype.toReadableString = function (fractionalDigits) {
    var str = this.toString();
    var fractionalPart;
    if (str.includes('.')) {
        var pointIndex = str.indexOf('.');
        fractionalPart = str.substring(pointIndex+1);
        if (fractionalPart.length > 3) {
            return str;
        }
        if(fractionalPart.length < fractionalDigits) {
            fractionalPart += '0'.repeat(fractionalDigits - fractionalPart.length);
        }
        fractionalPart = '.' + fractionalPart;
        str = str.substring(0, pointIndex);
    }

    var sub = str.substring(str.length - 3, str.length);
    var newstr = sub;
    var i = 1;

    while (sub.length >= 3) {
        sub = str.substring(str.length - ((i + 1) * 3), str.length - (i * 3));
        newstr = sub + ' ' + newstr;
        i += 1;
    }
    if(newstr.startsWith(' ')) {
        newstr = newstr.substring(1);
    }
    return newstr + (fractionalPart || '');
}

function getProperty(object, property, defaultValue) {
    return object[property] ? object[property] : defaultValue;
}

/*function printf(s) {
    print(s.format(Object.values(arguments).slice(1)));
}*/