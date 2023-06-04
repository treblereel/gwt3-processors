package ${package};

import org.treblereel.j2cl.processors.common.resources.ResourcePrototype;
import org.treblereel.j2cl.processors.common.resources.TextResource;

public class ${className} implements ${parent} {

    public static final ${className} INSTANCE = new ${className}();

    private java.util.HashMap<String, ResourcePrototype> resourceMap;

    private ${className}() {

    }

<#list methods as method>
    ${method?no_esc};
</#list>

    public ResourcePrototype getResource(String name) {
        if (resourceMap == null) {
            resourceMap = new java.util.HashMap<>();
<#list resources as resource>
            resourceMap.put("${resource}", ${resource}());
</#list>
        }
        return resourceMap.get(name);
    }
}
