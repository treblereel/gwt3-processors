        ${name} = new org.treblereel.j2cl.processors.common.resources.DataResource() {
                            public String asString() {
                                ${impl?no_esc}
                            }
                            public String getName() {
                                return "${name}";
                            }
        };