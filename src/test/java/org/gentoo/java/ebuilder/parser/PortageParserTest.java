package org.gentoo.java.ebuilder.parser;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.gentoo.java.ebuilder.model.EbuildModel;
import org.gentoo.java.ebuilder.model.MavenCoordinates;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class PortageParserTest {

    @Inject
    PortageParser portageParser;

    @InjectMock
    EbuildParser ebuildParser;

    @BeforeEach
    public void setup() {
        Mockito.when(ebuildParser
                .parseEbuild(Mockito.any(Path.class)))
                .thenReturn(Optional.of(new EbuildModel("a", "a", "1.0.0", "1", "doc", new MavenCoordinates(), List.of(""))))
                .thenReturn(Optional.of(new EbuildModel("b", "b", "2.0.0", "2", "java", new MavenCoordinates(), List.of("ant"))))
                .thenReturn(Optional.of(new EbuildModel("c", "c", "3.0.0", "3", "X", new MavenCoordinates(), List.of("java"))));
    }

    @Test
    public void parsingPortageTreeShouldParseAllEbuilds() throws IOException {
        Path portageRootDirectory = Path.of("src/test/resources/portage");

        portageParser.parseTreeForEbuilds(portageRootDirectory);

        Assertions.assertEquals(3, EbuildModel.count());
    }
}