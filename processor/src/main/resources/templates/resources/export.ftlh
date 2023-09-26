goog.module('${module}$GWT3Export');

const EXPORT = goog.require('${target}');

<#if isNative>
goog.exportSymbol('${type}', EXPORT);
<#else>
const j_l_Object = goog.require('java.lang.Object$impl');
const $Util = goog.require('nativebootstrap.Util$impl');

class _EXPORT extends EXPORT {
    constructor() {
        EXPORT.$clinit();
        super();
        this.${ctor}();
    }
}

$Util.$setClassMetadata(_EXPORT, '${module}');
goog.exportSymbol('${type}', _EXPORT);
</#if>

<#list methods as method>
goog.exportSymbol('${type}.<#if !method.isStatic>prototype.</#if>${method.name}', EXPORT.<#if !method.isStatic>prototype.</#if>${method.mangleName});
</#list>
