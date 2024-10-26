package org.gentoo.java.ebuilder.parser;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.gentoo.java.ebuilder.exception.EmptyEbuildException;
import org.gentoo.java.ebuilder.exception.MalformedEbuildException;
import org.gentoo.java.ebuilder.model.EbuildModel;
import org.gentoo.java.ebuilder.model.MavenCoordinates;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class EbuildParser {

    private static final Logger LOG = Logger.getLogger(EbuildParser.class);

    @ConfigProperty(name = "eclass.ant_tasks")
    String ECLASS_ANT_TASKS;

    @ConfigProperty(name = "eclass.java_pkg_opt")
    String ECLASS_JAVA_PKG_OPT;

    public Optional<EbuildModel> parseFile(String pathString) throws EmptyEbuildException, IOException {
        List<String> ebuildContent = readFile(pathString);
        // TODO: check if list is empty, if so skip next steps

        EbuildModel ebuild = new EbuildModel();

        ebuild.category = parseCategory(pathString);
        ebuild.pn = parsePN(pathString);
        ebuild.pvr = parsePVR(pathString, ebuild.pn);

        ebuildContent = substituteEbuildVariablesWithValues(ebuildContent, ebuild);

        ebuild.javaEclass = parseEclasses(ebuildContent);
        ebuild.slot = parseSlot(ebuildContent);
        ebuild.useFlag = parseUseFlags(ebuildContent).orElse("");
        ebuild.mavenCoordinates = parseMavenCoordinates(ebuildContent);

        return Optional.of(ebuild);
    }


    private List<String> readFile(String pathString) throws IOException {
        Path filePath = Paths.get(pathString);
        try {

            return Files.readAllLines(filePath)
                    .stream()
                    .filter(Predicate.not(String::isEmpty))
                    .filter(Predicate.not(String::isBlank))
                    .filter(Predicate.not(line -> line.startsWith("#")))
                    .map(String::trim)
                    .toList();

        } catch (IOException e) {
            LOG.error("Could not read File: '" + pathString + "'. Therefore skipping ...");
            throw e;
        }
    }

    private List<String> substituteEbuildVariablesWithValues(List<String> ebuildContent, EbuildModel ebuildModel) {
        return ebuildContent.stream()
                .map(s -> s.replaceAll("\\$\\{PN.*}", ebuildModel.pn))
                .map(s -> s.replaceAll("\\$\\{PVR.*}", ebuildModel.pvr))
                .map(s -> s.replaceAll("\\$\\{PV.*}", ebuildModel.getPV()))
                .toList();
    }

    private List<String> parseEclasses(List<String> ebuildContent) {
        // TODO:  if (eclasses.contains(ECLASS_JAVA_PKG_OPT) && useFlag == null)
//        Stream<String[]> a = ebuildContent.stream()
//                .filter(s -> s.startsWith("inherit"))
//                .map(s -> s.split(" "))
//                .flatMap(a -> Arrays.stream(a).sequential())
//                .filter(Predicate.not(s -> s.startsWith("inherit")))
//                .filter(eclass -> ECLASS_JAVA_PKG_OPT.equals(eclass))


        return ebuildContent.stream()
                .filter(s -> s.startsWith("inherit"))
                .map(s -> s.split(" "))
                .flatMap(a -> Arrays.stream(a).sequential())
                .filter(Predicate.not(s -> s.startsWith("inherit")))
                .filter(eclass -> eclass.startsWith("java-") || ECLASS_ANT_TASKS.equals(eclass))
                .collect(Collectors.toList());
    }

    private String parseCategory(String pathString) {
        String[] a = pathString.split(Pattern.quote(File.separator));
        return a[a.length - 3];
    }

    private String parsePN(String pathString) {
        String[] a = pathString.split(Pattern.quote(File.separator));
        return a[a.length - 2];
    }

    private String parsePVR(String pathString, String packageName) {
        String filename = Path.of(pathString).getFileName().toString();

        return filename
                .replaceAll(packageName + "-", "")
                .replaceAll(".ebuild", "");
    }

    private String parseSlot(List<String> ebuildContent) throws MalformedEbuildException {
        return ebuildContent.stream()
                .filter(line -> line.startsWith("SLOT"))
                .findFirst()
                .map(slot -> slot.split("=")[1])
                .map(String::trim)
                .map(slot -> slot.replaceAll("\"", ""))
                .orElseThrow(MalformedEbuildException::new);
    }

    private Optional<String> parseUseFlags(List<String> ebuildContent) {
        return ebuildContent.stream()
                .filter(line -> line.startsWith("JAVA_PKG_OPT_USE"))
                .findFirst()
                .map(useflags -> useflags.split("=")[1])
                .map(String::trim)
                .map(useflags -> useflags.replaceAll("\"", ""));
    }

    private List<MavenCoordinates> parseMavenCoordinates(List<String> ebuildContent) {
        return parseMavenCoordinatesFromMavenId(ebuildContent)
                .map(List::of)
                .orElseGet(() -> parseMavenCoordinatesFromMavenProvides(ebuildContent));
    }

    private Optional<MavenCoordinates> parseMavenCoordinatesFromMavenId(List<String> ebuildContent) {
        return ebuildContent.stream()
                .dropWhile(Predicate.not(s -> s.startsWith("MAVEN_ID=")))
                .takeWhile(Predicate.not(s -> s.equals("\"")))
                .map(s -> s.replaceAll("MAVEN_ID=\"", ""))
                .map(s -> s.replaceAll("\"", ""))
                .filter(Predicate.not(String::isEmpty))
                .filter(Predicate.not(String::isBlank))
                .map(MavenCoordinates::new)
                .findFirst();
    }

    private List<MavenCoordinates> parseMavenCoordinatesFromMavenProvides(List<String> ebuildContent) {
        return ebuildContent.stream()
                .dropWhile(Predicate.not(s -> s.startsWith("MAVEN_PROVIDES=")))
                .takeWhile(Predicate.not(s -> s.equals("\"")))
                .map(s -> s.replaceAll("MAVEN_PROVIDES=\"", ""))
                .filter(Predicate.not(String::isEmpty))
                .filter(Predicate.not(String::isBlank))
                .map(MavenCoordinates::new)
                .collect(Collectors.toList());

    }
}
