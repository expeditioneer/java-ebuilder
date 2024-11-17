package org.gentoo.java.ebuilder.converter;

import org.eclipse.microprofile.config.spi.Converter;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class PathListConverter implements Converter<List<Path>> {
    @Override
    public List<Path> convert(String value) {
        if (value.isBlank()) {
            return Collections.emptyList();
        } else {
            return Stream.of(value.split(","))
                    .distinct()
                    .map(Path::of)
                    .toList();
        }
    }
}
