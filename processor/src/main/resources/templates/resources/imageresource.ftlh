        ${encoded?no_esc}
        ${name} =  new org.treblereel.j2cl.processors.common.resources.impl.ImageResourcePrototype(
                            "${name}",
                            encoded,
                            ${width?no_esc}, ${height?no_esc});