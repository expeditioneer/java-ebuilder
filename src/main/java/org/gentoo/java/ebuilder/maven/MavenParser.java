package org.gentoo.java.ebuilder.maven;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.gentoo.java.ebuilder.Config;
import org.gentoo.java.ebuilder.parser.XMLUtilities;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class MavenParser {
    private static final Logger LOG = Logger.getLogger(MavenParser.class);

    @Inject
    XMLUtilities xmlUtilities;

    @Inject
    MavenXpp3Reader mavenXpp3Reader;

    public List<Model> parsePomFile(Path pomXmlPath) {

        List<Model> mavenModels = new ArrayList<>();
        File effectivePom = getEffectivePom(pomXmlPath);

        Document document = parsePOMtoDOMDocument(effectivePom);

        if ("projects".equals(document.getDocumentElement().getNodeName())) {
            LOG.info("Effective POM contains multiple projects");
            NodeList nodeList = document.getElementsByTagName("project");

            for (int i = 0; i < nodeList.getLength(); i++) {
                DocumentBuilder builder = createDocumentBuilder(true);
                Document mavenSingleProjectDocument = builder.newDocument();

                Node importedNode = mavenSingleProjectDocument.importNode(nodeList.item(i), true);
                mavenSingleProjectDocument.appendChild(importedNode);

                mavenModels.add(readMavenProject(mavenSingleProjectDocument));
            }
        } else if ("project".equals(document.getDocumentElement().getNodeName())) {
            LOG.info("Effective POM contains single project");
            mavenModels.add(readMavenProject(effectivePom));
        }

        return mavenModels;
    }

    /**
     * Stores effective pom to file and returns the file.
     *
     * @param pomXmlPath path to pom.xml file that should be processed
     * @return path to effective pom
     */
    File getEffectivePom(Path pomXmlPath) {
        File effectivePom;

        try {
            effectivePom = File.createTempFile("effective-pom-", ".xml");
        } catch (IOException e) {
            LOG.error("Failed to create temporary file for effective pom", e);
            return null;
        }

        LOG.info("Retrieving effective pom for " + pomXmlPath + " into " + effectivePom + "...");

        // TODO: clarify if .setQuiet() should be added
        EmbeddedMaven.forProject(pomXmlPath.toFile())
                .addProperty("output", effectivePom.toString())
                .setGoals("help:effective-pom")
                .build();

        LOG.info("... done");

        return effectivePom;
    }

    Document parsePOMtoDOMDocument(File effectivePom) {
        DocumentBuilder documentBuilder = createDocumentBuilder(false);
        try {
            Document document = documentBuilder.parse(effectivePom);
            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            document.getDocumentElement().normalize();

            return document;
        } catch (SAXException e) {
            LOG.error("Parsing error occurred", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            LOG.error("Input output error occurred", e);
            throw new RuntimeException(e);
        }
    }

    DocumentBuilder createDocumentBuilder(boolean namespaceAware) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            // optional, but recommended - process XML securely, avoid attacks like XML External Entities (XXE)
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            documentBuilderFactory.setNamespaceAware(namespaceAware);

            return documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            LOG.error("Could not create XML Document Builder", e);
            throw new RuntimeException(e);
        }
    }

    Model readMavenProject(Document document) {
        InputStream mavenProject = xmlUtilities.createInputStream(document);
        return readMavenProject(mavenProject);
    }

    Model readMavenProject(File file) {
        try {
            return readMavenProject(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    Model readMavenProject(InputStream inputStream) {
        try {
            return mavenXpp3Reader.read(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Parses specified pom.xml files.
     *
     * @param config     application configuration
     * @param mavenCache maven cache
     * @return list of maven projects
     */
    public List<MavenProject> parsePomFiles(Config config, MavenCache mavenCache) {
        List<MavenProject> result = new ArrayList<>(config.getPomFiles().size());

        config.getPomFiles().stream()
                .forEach((pomFile) -> {
                    File effectivePom = getEffectivePom(pomFile);

                    MavenProject mavenProject = parsePom(mavenCache, pomFile, effectivePom);

                    // TODO: I suppose they should go to "POJO" tests
                    if (mavenProject.hasTests() && mavenProject.getTestDependencies().isEmpty()) {
                        mavenProject.addDependency(new MavenDependency("junit", "junit", "4.11", "test", mavenCache.getDependency("junit", "junit", "4.11")));
                    }

                    if (config.hasTestSrcUri()) {
                        mavenProject.setHasTests(true);
                    }

                    if (config.willSkipTests()) {
                        mavenProject.setHasTests(false);
                    }

                    result.add(mavenProject);
                });

        return result;
    }

    /**
     * Consumes current element.
     *
     * @param reader XML stream reader
     * @throws XMLStreamException Thrown if problem occurred while parsing the element.
     */
    void consumeElement(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            reader.next();

            if (reader.isStartElement()) {
                consumeElement(reader);
            } else if (reader.isEndElement()) {
                return;
            }
        }
    }

    /**
     * Parses build plugin.
     *
     * @param mavenProject maven project instance
     * @param reader       XML stream reader
     * @throws XMLStreamException Thrown if problem occurred while reading XML stream.
     */
    private void parseBuildPlugin(MavenProject mavenProject, XMLStreamReader reader) throws XMLStreamException {
        String artifactId = null;

        while (reader.hasNext()) {
            reader.next();

            if (reader.isStartElement()) {
                switch (reader.getLocalName()) {
                    case "artifactId":
                        artifactId = reader.getElementText();
                        break;
                    case "configuration":
                        parseBuildPluginConfiguration(mavenProject, reader,
                                artifactId);
                        break;
                    default:
                        consumeElement(reader);
                }
            } else if (reader.isEndElement()) {
                return;
            }
        }
    }

    /**
     * Parses build plugin configuration.
     *
     * @param mavenProject maven project instance
     * @param reader       XML stream reader
     * @param artifactId   plugin artifact id
     * @throws XMLStreamException Thrown if problem occurred while reading XML stream.
     */
    private void parseBuildPluginConfiguration(MavenProject mavenProject, XMLStreamReader reader, String artifactId) throws XMLStreamException {
        while (reader.hasNext()) {
            reader.next();

            if (reader.isStartElement()) {
                switch (reader.getLocalName()) {
                    case "archive":
                        if ("maven-jar-plugin".equals(artifactId)) {
                            parseBuildPluginConfigurationArchive(mavenProject,
                                    reader);
                        } else {
                            consumeElement(reader);
                        }

                        break;
                    case "source":
                        if ("maven-compiler-plugin".equals(artifactId)) {
                            mavenProject.setSourceVersion(new JavaVersion(reader.getElementText()));
                        } else {
                            consumeElement(reader);
                        }

                        break;
                    case "target":
                        if ("maven-compiler-plugin".equals(artifactId)) {
                            mavenProject.setTargetVersion(new JavaVersion(reader.getElementText()));
                        } else {
                            consumeElement(reader);
                        }

                        break;
                    default:
                        consumeElement(reader);
                }
            } else if (reader.isEndElement()) {
                return;
            }
        }
    }

    /**
     * Parses archive element of build plugin configuration.
     *
     * @param mavenProject maven project instance
     * @param reader       XML stream reader
     * @throws XMLStreamException Thrown if problem occurred while reading XML stream.
     */
    private void parseBuildPluginConfigurationArchive(MavenProject mavenProject, XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            reader.next();

            if (reader.isStartElement()) {
                switch (reader.getLocalName()) {
                    case "manifest":
                        parseManifest(mavenProject, reader);
                        break;
                    default:
                        consumeElement(reader);
                }
            } else if (reader.isEndElement()) {
                return;
            }
        }
    }

    /**
     * Parses build plugins and its sub-elements.
     *
     * @param mavenProject maven project instance
     * @param reader       XML stream reader
     * @throws XMLStreamException Thrown if problem occurred while reading XML stream.
     */
    private void parseBuildPlugins(MavenProject mavenProject, XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            reader.next();

            if (reader.isStartElement()) {
                switch (reader.getLocalName()) {
                    case "plugin":
                        parseBuildPlugin(mavenProject, reader);
                        break;
                    default:
                        consumeElement(reader);
                }
            }
        }
    }

    /**
     * Parses manifest elements.
     *
     * @param mavenProject maven project instance
     * @param reader       XML stream reader
     * @throws XMLStreamException Thrown if problem occurred while reading XML stream.
     */
    private void parseManifest(MavenProject mavenProject, XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            reader.next();

            if (reader.isStartElement()) {
                switch (reader.getLocalName()) {
                    case "mainClass":
                        mavenProject.setMainClass(reader.getElementText());
                        break;
                    default:
                        consumeElement(reader);
                }
            } else if (reader.isEndElement()) {
                return;
            }
        }
    }

    /**
     * Parses the pom file and returns maven project instance containing collected information.
     *
     * @param mavenCache   maven cache
     * @param pomFile      path to pom.xml file
     * @param effectivePom path to effective pom
     * @return maven project instance
     */
    private MavenProject parsePom(MavenCache mavenCache, Path pomFile, File effectivePom) {
        LOG.info("Parsing effective pom...");

        XMLStreamReader reader;

        try {
            reader = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(effectivePom));
        } catch (FactoryConfigurationError | FileNotFoundException | XMLStreamException e) {
            throw new RuntimeException("Failed to read effective pom", e);
        }

        MavenProject mavenProject = new MavenProject(pomFile);

        try {
            while (reader.hasNext()) {
                reader.next();

                if (reader.isStartElement()) {
                    switch (reader.getLocalName()) {
                        case "projects":
                            /* no-op */
                            break;
                        case "project":
                            parseProject(mavenProject, mavenCache, reader);
                            break;
                        default:
                            consumeElement(reader);
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to parse effective pom", e);
        }

        LOG.info("done");

        return mavenProject;
    }

    /**
     * Parses project element and it's sub-elements.
     *
     * @param mavenProject maven project instance
     * @param reader       XML stream reader
     * @throws XMLStreamException Thrown if problem occurred while reading XML stream.
     */
    private void parseProject(MavenProject mavenProject, MavenCache mavenCache, XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            reader.next();

            if (reader.isStartElement()) {
                switch (reader.getLocalName()) {
                    case "artifactId":
                        mavenProject.setArtifactId(reader.getElementText());
                        break;
                    case "build":
                        parseProjectBuild(mavenProject, reader);
                        break;
                    case "dependencies":
                        parseProjectDependencies(mavenProject, mavenCache,
                                reader);
                        break;
                    case "description":
                        mavenProject.setDescription(reader.getElementText());
                        break;
                    case "groupId":
                        mavenProject.setGroupId(reader.getElementText());
                        break;
                    case "licenses":
                        parseProjectLicenses(mavenProject, reader);
                        break;
                    case "properties":
                        parseProjectProperties(mavenProject, reader);
                        break;
                    case "url":
                        mavenProject.setHomepage(reader.getElementText());
                        break;
                    case "version":
                        mavenProject.setVersion(reader.getElementText().replace(
                                "-SNAPSHOT", ""));
                        break;
                    default:
                        consumeElement(reader);
                }
            } else if (reader.isEndElement()) {
                return;
            }
        }
    }

    /**
     * Parses project build element and its sub-elements.
     *
     * @param mavenProject maven project instance
     * @param reader       XML stream reader
     * @throws XMLStreamException Thrown if problem occurred while reading XML stream.
     */
    private void parseProjectBuild(MavenProject mavenProject, XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            reader.next();

            if (reader.isStartElement()) {
                switch (reader.getLocalName()) {
                    case "plugins":
                        parseBuildPlugins(mavenProject, reader);
                        break;
                    case "resources":
                        parseResources(mavenProject, reader);
                        break;
                    case "sourceDirectory":
                        mavenProject.setSourceDirectory(
                                Paths.get(reader.getElementText()));
                        break;
                    case "testResources":
                        parseTestResources(mavenProject, reader);
                        break;
                    case "testSourceDirectory":
                        mavenProject.setTestSourceDirectory(
                                Paths.get(reader.getElementText()));
                        break;
                    default:
                        consumeElement(reader);
                }
            } else if (reader.isEndElement()) {
                return;
            }
        }
    }

    /**
     * Parses project dependencies and its sub-elements.
     *
     * @param mavenProject maven project instance
     * @param mavenCache   maven cache
     * @param reader       XML stream reader
     * @throws XMLStreamException Thrown if problem occurred while reading XML stream.
     */
    private void parseProjectDependencies(MavenProject mavenProject, MavenCache mavenCache, XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            reader.next();

            if (reader.isStartElement()) {
                switch (reader.getLocalName()) {
                    case "dependency":
                        parseProjectDependency(mavenProject, mavenCache, reader);
                        break;
                    default:
                        consumeElement(reader);
                }
            } else if (reader.isEndElement()) {
                return;
            }
        }
    }

    /**
     * Parses project dependency.
     *
     * @param mavenProject maven project instance
     * @param mavenCache   maven cache
     * @param reader       XML stream reader
     * @throws XMLStreamException Thrown if problem occurred while reading XML stream.
     */
    private void parseProjectDependency(MavenProject mavenProject, MavenCache mavenCache, XMLStreamReader reader) throws XMLStreamException {
        String groupId = null;
        String artifactId = null;
        String version = null;
        String scope = "compile";

        while (reader.hasNext()) {
            reader.next();

            if (reader.isStartElement()) {
                switch (reader.getLocalName()) {
                    case "artifactId":
                        artifactId = reader.getElementText();
                        break;
                    case "groupId":
                        groupId = reader.getElementText();
                        break;
                    case "scope":
                        scope = reader.getElementText();
                        break;
                    case "version":
                        version = reader.getElementText().replace("-SNAPSHOT", "");

                        /* crazy version from
                         * org.khronos:opengl-api:gl1.1-android-2.1_r1 */
                        // TODO: this should go to a file mapping crazy versions
                        if (version.equals("gl1.1-android-2.1_r1")) {
                            version = "2.1.1";
                        }
                        break;
                    default:
                        consumeElement(reader);
                }
            } else if (reader.isEndElement()) {
                mavenProject.addDependency(new MavenDependency(groupId, artifactId, version, scope, mavenCache.getDependency(groupId, artifactId, version)));

                return;
            }
        }
    }

    /**
     * Parses project licenses.
     *
     * @param mavenProject maven project instance
     * @param reader       XML stream reader
     * @throws XMLStreamException Thrown if problem occurred while reading the XML stream.
     */
    private void parseProjectLicenses(MavenProject mavenProject, XMLStreamReader reader) throws XMLStreamException {
        MavenLicenses mavenLic = new MavenLicenses();

        while (reader.hasNext()) {
            reader.next();

            if (reader.isStartElement()) {
                switch (reader.getLocalName()) {
                    case "license":
                        parseProjectLicense(mavenLic, mavenProject, reader);
                        break;
                    default:
                        consumeElement(reader);
                }
            } else if (reader.isEndElement()) {
                return;
            }
        }
    }

    /**
     * Parses project license.
     *
     * @param mavenProject maven project instance
     * @param reader       XML stream reader
     * @throws XMLStreamException Thrown if problem occurred while reading the XML stream.
     */
    private void parseProjectLicense(MavenLicenses mavenLicenses, MavenProject mavenProject, XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            reader.next();

            if (reader.isStartElement()) {
                switch (reader.getLocalName()) {
                    case "name":
                        mavenProject.addLicense(
                                mavenLicenses.getEquivalentLicense(
                                        reader.getElementText()));
                        break;
                    default:
                        consumeElement(reader);
                }
            } else if (reader.isEndElement()) {
                return;
            }
        }
    }

    /**
     * Parses project properties.
     *
     * @param mavenProject maven project instance
     * @param reader       XML stream reader
     * @throws XMLStreamException Thrown if problem occurred while reading the XML stream.
     */
    private void parseProjectProperties(MavenProject mavenProject, XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            reader.next();

            if (reader.isStartElement()) {
                switch (reader.getLocalName()) {
                    case "maven.compiler.source":
                        mavenProject.setSourceVersion(
                                new JavaVersion(reader.getElementText()));
                        break;
                    case "maven.compiler.target":
                        mavenProject.setTargetVersion(
                                new JavaVersion(reader.getElementText()));
                        break;
                    case "project.build.sourceEncoding":
                        mavenProject.setSourceEncoding(reader.getElementText());
                        break;
                    default:
                        consumeElement(reader);
                }
            } else if (reader.isEndElement()) {
                return;
            }
        }
    }

    /**
     * Parses resource element.
     *
     * @param mavenProject maven project instance
     * @param reader       XML stream reader
     * @throws XMLStreamException Thrown if problem occurred while reading XML stream.
     */
    private void parseResource(MavenProject mavenProject, XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            reader.next();

            if (reader.isStartElement()) {
                switch (reader.getLocalName()) {
                    case "directory":
                        mavenProject.addResourceDirectory(
                                Paths.get(reader.getElementText()));
                        break;
                    default:
                        consumeElement(reader);
                }
            } else if (reader.isEndElement()) {
                return;
            }
        }
    }

    /**
     * Parses resources and its sub-elements.
     *
     * @param mavenProject maven project instance
     * @param reader       XML stream reader
     * @throws XMLStreamException Thrown if problem occurred while reading XML stream.
     */
    private void parseResources(MavenProject mavenProject, XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            reader.next();

            if (reader.isStartElement()) {
                switch (reader.getLocalName()) {
                    case "resource":
                        parseResource(mavenProject, reader);
                        break;
                    default:
                        consumeElement(reader);
                }
            } else if (reader.isEndElement()) {
                return;
            }
        }
    }

    /**
     * Parses test resource.
     *
     * @param mavenProject maven project instance
     * @param reader       XML stream reader
     * @throws XMLStreamException Thrown if problem occurred while reading XML
     *                            stream.
     */
    private void parseTestResource(MavenProject mavenProject, XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            reader.next();

            if (reader.isStartElement()) {
                switch (reader.getLocalName()) {
                    case "directory":
                        mavenProject.addTestResourceDirectory(
                                Paths.get(reader.getElementText()));
                        break;
                    default:
                        consumeElement(reader);
                }
            } else if (reader.isEndElement()) {
                return;
            }
        }
    }

    /**
     * Parses test resources and its sub-elements.
     *
     * @param mavenProject maven project instance
     * @param reader       XML stream reader
     * @throws XMLStreamException Thrown if problem occurred while reading XML stream.
     */
    private void parseTestResources(MavenProject mavenProject, XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            reader.next();

            if (reader.isStartElement()) {
                switch (reader.getLocalName()) {
                    case "testResource":
                        parseTestResource(mavenProject, reader);
                        break;
                    default:
                        consumeElement(reader);
                }
            } else if (reader.isEndElement()) {
                return;
            }
        }
    }
}
