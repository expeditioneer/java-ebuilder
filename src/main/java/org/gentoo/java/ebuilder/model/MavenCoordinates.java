package org.gentoo.java.ebuilder.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import org.jboss.logging.Logger;

import java.util.Objects;

@Entity
public class MavenCoordinates extends PanacheEntity {
    private static final Logger LOG = Logger.getLogger(MavenCoordinates.class);

    public String groupId;
    public String artifactId;
    public String version;

    public MavenCoordinates() {}

    public MavenCoordinates(String gav) {
        String[] a = gav.split(":");

        if(a.length != 3) {
            LOG.error("Given GAV are not valid: " + gav);
            throw new IllegalArgumentException();
        }

        this.groupId = a[0];
        this.artifactId = a[1];
        this.version = a[2];
    }

    public MavenCoordinates(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    @Override
    public String toString() {
        return "MavenCoordinates{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MavenCoordinates that)) return false;

        return Objects.equals(groupId, that.groupId) &&
                Objects.equals(artifactId, that.artifactId) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(groupId);
        result = 31 * result + Objects.hashCode(artifactId);
        result = 31 * result + Objects.hashCode(version);
        return result;
    }
}
