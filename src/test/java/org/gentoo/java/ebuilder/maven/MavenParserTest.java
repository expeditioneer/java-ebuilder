package org.gentoo.java.ebuilder.maven;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

@QuarkusTest
class MavenParserTest {

    @Inject
    MavenParser mavenParser;

    @Test
    public void testXMLWhichContainsProjectsRootElement() {
        Path pomXmlPath = Path.of("src/test/resources/maven/apache-log4j-2.15.0-src/pom.xml");
        List<Model> mavenModels  = mavenParser.parsePomFile(pomXmlPath);

        Assertions.assertEquals(45, mavenModels.size());
    }

    @Test
    public void testXMLWhichContainsNOProjectsRootElement() {
        Path pomXmlPath = Path.of("src/test/resources/maven/simple-project/pom.xml");
        List<Model> mavenModels  = mavenParser.parsePomFile(pomXmlPath);

        Assertions.assertEquals(1, mavenModels.size());
    }

    @Test
    public void testMavenXpp3Parser() throws IOException, XmlPullParserException {
        MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
        Model model = mavenXpp3Reader.read(Files.newInputStream(new File("src/test/resources/maven/apache-log4j-2.15.0-src/pom.xml").toPath()), false);

        Properties properties = model.getProperties();
        List<Dependency> dependencies = model.getDependencyManagement().getDependencies();
        List<Plugin> plugins = model.getBuild().getPluginManagement().getPlugins();

        List<Dependency> testDependencies = dependencies.stream().filter(d -> "test".equals(d.getScope())).toList();

        Assertions.assertNotNull(properties);
        Assertions.assertNotNull(dependencies);
        Assertions.assertNotNull(plugins);
    }
}