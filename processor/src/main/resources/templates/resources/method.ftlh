private static ${prototype} ${name};

    private void ${name}Initializer() {
        ${initializer?no_esc}
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