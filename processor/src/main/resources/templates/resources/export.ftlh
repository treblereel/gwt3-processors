goog.module('${module}$GWT3Export');

goog.require('java.lang.Object');
goog.require('nativebootstrap.Util');

const EXPORT = goog.require('${target}');

class _EXPORT extends EXPORT {
    constructor() {
        super();
        this.${ctor}();
    }
}

goog.exportSymbol('${type}', _EXPORT);
goog.exportSymbol('${type}.prototype.constructor', EXPORT.$create__());


<#list methods as method>
    goog.exportSymbol('${type}.<#if !method.isStatic>prototype.</#if>${method.name}', _EXPORT.<#if !method.isStatic>prototype.</#if>${method.mangleName});
</#list>

exports = _EXPORT;
