package org.gentoo.java.ebuilder.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

public class MavenProducers {

    @ApplicationScoped
    MavenXpp3Reader produceMavenXpp3Reader() {
        return new MavenXpp3Reader();
    }
}
