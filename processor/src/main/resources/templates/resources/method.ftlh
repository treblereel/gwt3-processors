private static ${prototype} ${name};

    private void ${name}Initializer() {
        ${name} = new ${prototype}() {
            public String ${getter}() {
                ${initializer?no_esc}
            }
            public String getName() {
                return "${name}";
            }
        };
    }

    private static class ${name}Initializer {
        static {
            INSTANCE.${name}Initializer();
        }
        static ${prototype} get() {
            return ${name};
        }
    }

    @Override
    public ${prototype} ${name}() {
        return ${name}Initializer.get();
    }
