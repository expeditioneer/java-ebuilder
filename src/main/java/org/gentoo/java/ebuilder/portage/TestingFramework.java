package org.gentoo.java.ebuilder.portage;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Dependency;

import java.util.Arrays;
import java.util.Optional;


/** TODO: missing determination for
 *    "POJO" tests
 *    "spock" tests
 */
public enum TestingFramework {
    junit("junit", true, "junit", "junit", "1.0.0", "3.9.9"),
    junit4("junit4", true, "junit", "junit", "4.0.0"),
    testng("testng", true, "org.testng", "testng"),

    // java-pkg-simple does not support this framework
    junit_jupiter("junit-jupiter", false, "org.junit.jupiter", "junit-jupiter-engine"),

    // java-pkg-simple does not support this framework
    junit_vintage("junit-vintage", false, "org.junit.vintage", "junit-vintage-engine"),

    // java-pkg-simple does not support this framework
    cucumber("cucumber", false, "io.cucumber", "cucumber-junit");

    private static final String DEFAULT_MIN_VERSION = "0.0.1";
    private static final String DEFAULT_MAX_VERSION = "999.999.999";

    public final String gentooJavaTestingFramework;
    public final boolean isSupportedByGentoo;
    public final String groupId;
    public final String artifactId;
    public final DefaultArtifactVersion supportedMinVersion;
    public final DefaultArtifactVersion supportedMaxVersion;

    TestingFramework(String gentooJavaTestingFramework, boolean isSupportedByGentoo, String groupId, String artifactId, String supportedMinVersion, String supportedMaxVersion) {
        this.gentooJavaTestingFramework = gentooJavaTestingFramework;
        this.isSupportedByGentoo = isSupportedByGentoo;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.supportedMinVersion = new DefaultArtifactVersion(supportedMinVersion);
        this.supportedMaxVersion = new DefaultArtifactVersion(supportedMaxVersion);
    }

    TestingFramework(String gentooJavaTestingFramework, boolean isSupportedByGentoo, String groupId, String artifactId, String supportedMinVersion) {
        this(gentooJavaTestingFramework, isSupportedByGentoo, groupId, artifactId, supportedMinVersion, DEFAULT_MAX_VERSION);
    }

    TestingFramework(String gentooJavaTestingFramework, boolean isSupportedByGentoo, String groupId, String artifactId) {
        this(gentooJavaTestingFramework, isSupportedByGentoo, groupId, artifactId, DEFAULT_MIN_VERSION, DEFAULT_MAX_VERSION);
    }

    public static Optional<TestingFramework> getMatchTestingFrameworkFrom(Dependency mavenDependency) {
        return Arrays.stream(TestingFramework.values())
                .filter(t -> t.groupId.equals(mavenDependency.getGroupId()))
                .filter(t -> t.artifactId.equals(mavenDependency.getArtifactId()))
                .filter(t -> isInSupportedVersionRange(mavenDependency,t))
                .findFirst();
    }

    private static boolean isInSupportedVersionRange(Dependency mavenDependency, TestingFramework testingFramework) {
        String desiredVersionString = mavenDependency.getVersion();
        DefaultArtifactVersion desiredVersion = new DefaultArtifactVersion(desiredVersionString);

        boolean greaterThanMinSupportedVersion = desiredVersion.compareTo(testingFramework.supportedMinVersion) >= 0;
        boolean lesserThanMaxSupportedVersion = desiredVersion.compareTo(testingFramework.supportedMaxVersion) <= 0;

        return greaterThanMinSupportedVersion && lesserThanMaxSupportedVersion;
    }
}
