package org.gentoo.java.ebuilder.configuration;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithConverter;
import org.gentoo.java.ebuilder.converter.PathConverter;
import org.gentoo.java.ebuilder.converter.PathListConverter;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@ConfigMapping(prefix = "portage")
public interface PortageConfiguration {

    @WithConverter(PathConverter.class)
    Path treeRootDirectory();

    @WithConverter(PathListConverter.class)
    Optional<List<Path>> additionalEbuildRepositories();
}
