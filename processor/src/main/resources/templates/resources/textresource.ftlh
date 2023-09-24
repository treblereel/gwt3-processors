        ${name} = new org.treblereel.j2cl.processors.common.resources.impl.TextResourcePrototype() {
                            public String getText() {
                                ${impl?no_esc}
                            }
                            public String getName() {
                                return "${name}";
                            }
                    };
