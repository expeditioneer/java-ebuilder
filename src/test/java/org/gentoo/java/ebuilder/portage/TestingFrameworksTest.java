package org.gentoo.java.ebuilder.portage;

import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.maven.model.Dependency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

@QuarkusTest
class TestingFrameworksTest {

    @ParameterizedTest
    @MethodSource("provideMavenDependencies")
    public void compareToMavenDependencyShouldWork(Dependency mavenDependency, TestingFramework expected) {
        Optional<TestingFramework> actual = TestingFramework.getMatchTestingFrameworkFrom(mavenDependency);

        Assertions.assertEquals(expected, actual.get());
    }

    private static Stream<Arguments> provideMavenDependencies() {
        return Stream.of(
                Arguments.of(
                        new DependencyBuilder()
                                .withGroupId("junit")
                                .withArtifactId("junit")
                                .withVersion("1.0.0")
                                .build(),
                        TestingFramework.junit
                ),
                Arguments.of(
                        new DependencyBuilder()
                                .withGroupId("junit")
                                .withArtifactId("junit")
                                .withVersion("3.9.9")
                                .build(),
                        TestingFramework.junit
                ),
                Arguments.of(
                        new DependencyBuilder()
                                .withGroupId("junit")
                                .withArtifactId("junit")
                                .withVersion("4.0.0")
                                .build(),
                        TestingFramework.junit4
                ),
                Arguments.of(
                        new DependencyBuilder()
                                .withGroupId("org.testng")
                                .withArtifactId("testng")
                                .withVersion("7.0.0-beta1")
                                .build(),
                        TestingFramework.testng

                ),
                Arguments.of(
                        new DependencyBuilder()
                                .withGroupId("org.junit.jupiter")
                                .withArtifactId("junit-jupiter-engine")
                                .withVersion("5.10.5")
                                .build(),
                        TestingFramework.junit_jupiter
                ),
                Arguments.of(
                        new DependencyBuilder()
                                .withGroupId("org.junit.vintage")
                                .withArtifactId("junit-vintage-engine")
                                .withVersion("5.11.0-M1")
                                .build(),
                        TestingFramework.junit_vintage
                ),
                Arguments.of(
                        new DependencyBuilder()
                                .withGroupId("io.cucumber")
                                .withArtifactId("cucumber-junit")
                                .withVersion("6.0.0-RC2")
                                .build(),
                        TestingFramework.cucumber
                )
        );
    }

    private static class DependencyBuilder {
        Dependency dependency;

        DependencyBuilder() {
            dependency = new Dependency();
        }

        DependencyBuilder withGroupId(String groupId) {
            dependency.setGroupId(groupId);
            return this;
        }

        DependencyBuilder withArtifactId(String artifactId) {
            dependency.setArtifactId(artifactId);
            return this;
        }

        DependencyBuilder withVersion(String version) {
            dependency.setVersion(version);
            return this;
        }

        Dependency build() {
            if (dependency.getGroupId() == null) {
                String message = "Group ID for " + this + " is not set";
                Log.error(message);
                throw new IllegalArgumentException(message);
            }
            if (dependency.getArtifactId() == null) {
                String message = "Artifact ID for " + this + " is not set";
                Log.error(message);
                throw new IllegalArgumentException(message);
            }
            if (dependency.getVersion() == null) {
                String message = "Version for " + this + " is not set";
                Log.error(message);
                throw new IllegalArgumentException(message);
            }
            return dependency;
        }

        @Override
        public String toString() {
            return "DependencyBuilder{" +
                    "dependency=" + dependency +
                    '}';
        }
    }
}


