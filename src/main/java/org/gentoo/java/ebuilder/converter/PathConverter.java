package org.gentoo.java.ebuilder.converter;

import org.eclipse.microprofile.config.spi.Converter;

import java.nio.file.Path;

public class PathConverter implements Converter<Path> {
    @Override
    public Path convert(String value) {
        return Path.of(value);
    }
}
