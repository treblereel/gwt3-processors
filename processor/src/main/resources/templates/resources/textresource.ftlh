        ${name} = new org.treblereel.j2cl.processors.common.resources.TextResource() {
                            public String getText() {
                                ${impl?no_esc}
                            }
                            public String getName() {
                                return "${name}";
                            }
                    };