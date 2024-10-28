package org.gentoo.java.ebuilder.parser;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.gentoo.java.ebuilder.model.EbuildModel;
import org.gentoo.java.ebuilder.model.MavenCoordinates;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@QuarkusTest
class EbuildParserTest {

    @Inject
    EbuildParser ebuildParser;

    @ParameterizedTest
    @MethodSource("provideEbuilds")
    public void parsingEbuildShouldReturnMatchingModel(String ebuildFilePath, EbuildModel expectedEbuildModel) throws IOException {
        Optional<EbuildModel> actual = ebuildParser.parseEbuild(ebuildFilePath);

        Assertions.assertEquals(expectedEbuildModel, actual.get());
    }

    private static Stream<Arguments> provideEbuilds() {
        return Stream.of(
                Arguments.of("src/test/resources/portage/dev-java/commons-imaging/commons-imaging-1.0_alpha3-r2.ebuild",
                        new EbuildModel("dev-java", "commons-imaging", "1.0_alpha3-r2", "1", "", new MavenCoordinates("org.apache.commons", "commons-imaging", "1.0_alpha3"), List.of("java-pkg-2", "java-pkg-simple"))

                ),

                Arguments.of("src/test/resources/portage/dev-java/icu4j/icu4j-75.1.ebuild",
                        new EbuildModel(
                                "dev-java",
                                "icu4j",
                                "75.1",
                                "0",
                                "",
                                List.of(
                                    new MavenCoordinates("com.ibm.icu", "icu4j", "75.1"),
                                    new MavenCoordinates("com.ibm.icu", "icu4j-charset", "75.1"),
                                    new MavenCoordinates("com.ibm.icu", "icu4j-localespi", "75.1")
                                ),
                                List.of("java-pkg-2", "java-pkg-simple")
                        )
                ),

                Arguments.of("src/test/resources/portage/dev-java/java-getopt/java-getopt-1.0.14-r3.ebuild",
                        new EbuildModel(
                                "dev-java",
                                "java-getopt",
                                "1.0.14-r3",
                                "1",
                                "",
                                new MavenCoordinates("gnu.getopt", "java-getopt", "1.0.14"),
                                List.of("java-pkg-2", "java-pkg-simple")
                        )
                )
        );
    }
}

