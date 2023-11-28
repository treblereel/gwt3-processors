<#if !isNative>
/**
* @fileoverview
* @suppress {missingProperties}
*/
</#if>
goog.module('${module}$GWT3Export');

const EXPORT = goog.require('${target}');

<#if isNative>
goog.exportSymbol('${type}', EXPORT);

<#list methods as method>
goog.exportSymbol('${type}.<#if !method.isStatic>prototype.</#if>${method.name}', EXPORT.<#if !method.isStatic>prototype.</#if>${method.mangleName});
</#list>
<#else>
const classProxy = new Proxy(EXPORT, {
    construct(target, args) {
        return EXPORT.$create__();
    }
});

EXPORT.$clinit();
goog.exportSymbol('${type}', classProxy);
<#list methods as method>
<#if method.isStatic>
goog.exportProperty(EXPORT, '${method.name}', EXPORT.${method.mangleName});
<#else>
goog.exportSymbol('${type}.prototype.${method.name}', classProxy.prototype.${method.mangleName});
</#if>
</#list>
</#if>
