package org.gentoo.java.ebuilder.startup;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.gentoo.java.ebuilder.configuration.PortageConfiguration;
import org.gentoo.java.ebuilder.parser.PortageParser;

import java.nio.file.Path;
import java.util.Collections;


@ApplicationScoped
public class InitializeEbuilds {

    @Inject
    PortageParser portageParser;

    @Inject
    PortageConfiguration configuration;

    @Startup
    void init() {
        Path portageRootDirectory = configuration.treeRootDirectory();

        Log.info("Parse Portage tree from " + portageRootDirectory + " for available Maven dependencies ...");
        portageParser.parseTreeForEbuilds(portageRootDirectory);
        Log.info("... parsing Portrage tree done.");

        configuration.additionalEbuildRepositories()
                .orElseGet(Collections::emptyList)
                .forEach(i -> {
                        Log.info("Parsing additional ebuild repository " + i + "for available Maven dependencies ...");
                        portageParser.parseTreeForEbuilds(i);
                        Log.info("... parsing additional ebuild repository done.");
                });
    }
}
