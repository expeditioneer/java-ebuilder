package org.gentoo.java.ebuilder.parser;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@ApplicationScoped
public class PortageParser {
    private static final Logger LOG = Logger.getLogger(PortageParser.class);

    @Inject
    EntityManager entityManager;

    @Inject
    EbuildParser ebuildParser;

    @Transactional
    public void parseTreeForEbuilds(Path portageRootDirectory) throws IOException {
        Files.walk(portageRootDirectory)
                .filter(Files::isRegularFile)
                .filter(PortageParser::isPathAnEbuild)
                .map(ebuildParser::parseEbuild)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(entityManager::persist);
    }

    private static boolean isPathAnEbuild(Path path) {
        return path.getFileName().toString().endsWith(".ebuild");
    }
}
