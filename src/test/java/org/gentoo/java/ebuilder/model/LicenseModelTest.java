package org.gentoo.java.ebuilder.model;

import io.quarkus.test.junit.QuarkusTest;
import org.gentoo.java.ebuilder.exception.MissingLicenseMappingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

@QuarkusTest
class LicenseModelTest {

    @ParameterizedTest
    @MethodSource("provideLicenses")
    public void licenseMappingFromMavenToGentooShouldWorkForKnownLicenses(String mavenLicense, String expectedGentooLicense) {
        Optional<LicenseModel> matchingLicenseModel = LicenseModel.findMatchingLicenseModelOptional(mavenLicense);

        if(matchingLicenseModel.isPresent()) {
            String actualGentooLicense = matchingLicenseModel.get().gentooLicense;
            Assertions.assertEquals(expectedGentooLicense, actualGentooLicense);
        }
        else throw new MissingLicenseMappingException();
    }


    @Test
    public void licenseMappingFromMavenToGentooShouldResultInEmptyOptionalForUnknownLicense() {
        Optional<LicenseModel> actualOptional = LicenseModel.findMatchingLicenseModelOptional("INVALID-LICENSE");

        Assertions.assertEquals(Optional.empty(), actualOptional);
    }

    private static Stream<Arguments> provideLicenses() {
        return Stream.of(
                Arguments.of("apache 2", "Apache-2.0"),
                Arguments.of("apache 2.0", "Apache-2.0"),
                Arguments.of("apache-2.0", "Apache-2.0"),
                Arguments.of("apache 2.0 license", "Apache-2.0"),
                Arguments.of("apache license", "Apache"),
                Arguments.of("apache license 2", "Apache-2.0"),
                Arguments.of("apache license 2.0", "Apache-2.0"),
                Arguments.of("apache license, 2.0", "Apache-2.0"),
                Arguments.of("apache license v2.0", "Apache-2.0"),
                Arguments.of("apache license version 2", "Apache-2.0"),
                Arguments.of("apache license, version 2.0", "Apache-2.0"),
                Arguments.of("apache license version 2.0", "Apache-2.0"),
                Arguments.of("apache license, version 2.0", "Apache-2.0"),
                Arguments.of("apache public license 2.0", "Apache-2.0"),
                Arguments.of("apache software license, version 1.1", "Apache-1.1"),
                Arguments.of("apache software license - version 2.0", "Apache-2.0"),
                Arguments.of("apache v2", "Apache-2.0"),
                Arguments.of("asf 2.0", "Apache-2.0"),
                Arguments.of("asl", "Apache-2.0"),
                Arguments.of("asl 2.0", "Apache-2.0"),
                Arguments.of("asl, version 2", "Apache-2.0"),
                Arguments.of("bsd", "BSD-1"),
                Arguments.of("bsd 2-clause", "BSD-2"),
                Arguments.of("bsd-2-clause", "BSD-2"),
                Arguments.of("bsd 2-clause license", "BSD-2"),
                Arguments.of("bsd 3-clause", "BSD"),
                Arguments.of("bsd-3-clause", "BSD"),
                Arguments.of("bsd licence", "BSD-1"),
                Arguments.of("bsd license", "BSD-1"),
                Arguments.of("bsd style", "BSD"),
                Arguments.of("cc0", "CC0-1.0"),
                Arguments.of("cddl 1.0", "CDDL"),
                Arguments.of("cddl 1.1", "CDDL-1.1"),
                Arguments.of("cddl+gpl license", "CDDL:GPL-1"),
                Arguments.of("cddl/gplv2+ce", "|| ( CDDL GPL-2-with-classpath-exception)"),
                Arguments.of("cddl + gplv2 with classpath exception", "CDDL:GPL-2-with-classpath-exception"),
                Arguments.of("cddl license", "CDDL"),
                Arguments.of("cddl v1.1 / gpl v2 dual license", "CDDL-1.1:GPL-2"),
                Arguments.of("common development and distribution license", "CDDL"),
                Arguments.of("common development and distribution license (cddl) plus gpl", "CDDL:GPL-1"),
                Arguments.of("common development and distribution license (cddl) v1.0", "CDDL"),
                Arguments.of("common development and distribution license (cddl) version 1.0", "CDDL"),
                Arguments.of("common public license - v 1.0", "CPL-1.0"),
                Arguments.of("common public license version 1.0", "CPL-1.0"),
                Arguments.of("dual license consisting of the cddl v1.1 and gpl v2", "CDDL-1.1:GPL-2"),
                Arguments.of("eclipse distribution license (edl), version 1.0", "EPL-1.0"),
                Arguments.of("eclipse distribution license - v 1.0", "EPL-1.0"),
                Arguments.of("eclipse distribution license v. 1.0", "EPL-1.0"),
                Arguments.of("eclipse public license", "EPL-1.0"),
                Arguments.of("eclipse public license 1.0", "EPL-1.0"),
                Arguments.of("eclipse public license 2.0", "EPL-2.0"),
                Arguments.of("eclipse public license (epl), version 1.0", "EPL-1.0"),
                Arguments.of("eclipse public license - v 1.0", "EPL-1.0"),
                Arguments.of("eclipse public license v1.0", "EPL-1.0"),
                Arguments.of("eclipse public license - v 2.0", "EPL-2.0"),
                Arguments.of("eclipse public license v. 2.0", "EPL-2.0"),
                Arguments.of("eclipse public license v2.0", "EPL-2.0"),
                Arguments.of("eclipse public license - version 1.0", "EPL-1.0"),
                Arguments.of("eclipse public license, version 1.0", "EPL-1.0"),
                Arguments.of("eclipse public license - version 2.0", "EPL-2.0"),
                Arguments.of("eclipse public license, version 2.0", "EPL-2.0"),
                Arguments.of("edl 1.0", "EPL-1.0"),
                Arguments.of("epl", "EPL-1.0"),
                Arguments.of("epl 2.0", "EPL-2.0"),
                Arguments.of("epl-2.0", "EPL-2.0"),
                Arguments.of("gnu general lesser public license (lgpl) version 3.0", "LGPL-3"),
                Arguments.of("gnu general public library", "GPL-1"),
                Arguments.of("gnu general public license, version 2", "GPL-2"),
                Arguments.of("gnu general public license version 2", "GPL-2"),
                Arguments.of("gnu general public license, version 2 with the classpath exception", "GPL-2-with-classpath-exception"),
                Arguments.of("gnu general public license, version 2, with the classpath exception", "GPL-2-with-classpath-exception"),
                Arguments.of("gnu general public license, version 2 with the gnu classpath exception", "GPL-2-with-classpath-exception"),
                Arguments.of("gnu lesser general public licence", "LGPL-3"),
                Arguments.of("gnu lesser general public license", "LGPL-3"),
                Arguments.of("gnu lesser general public license as published by the free software foundation; either version 2.1 of the license, or (at your option) any later version.", "LGPL-2.1"),
                Arguments.of("gnu lesser general public license (lgpl)", "LGPL-3"),
                Arguments.of("gnu lesser general public license (lgpl), version 2.1", "LGPL-2.1"),
                Arguments.of("gnu lesser general public license v2.1", "LGPL-2.1"),
                Arguments.of("gnu lesser general public license version 2.1", "LGPL-2.1"),
                Arguments.of("gnu lesser general public license, version 2.1", "LGPL-2.1"),
                Arguments.of("gnu lesser public license", "LGPL-3"),
                Arguments.of("gnu lgplv3", "LGPL-3"),
                Arguments.of("gnu library general public license v2.1 or later", "LGPL-2.1"),
                Arguments.of("go license", "BSD"),
                Arguments.of("gpl-2.0", "GPL-2"),
                Arguments.of("gpl-2.0-with-classpath-exception", "GPL-2-with-classpath-exception"),
                Arguments.of("gpl2 w/ cpe", "GPL-2-with-classpath-exception"),
                Arguments.of("gpl 3", "GPL-3"),
                Arguments.of("gpl v2", "GPL-2"),
                Arguments.of("gpl v2 with classpath exception", "GPL-2-with-classpath-exception"),
                Arguments.of("hsqldb license", "BSD"),
                Arguments.of("indiana university extreme! lab software license", "Indiana-University-Extreme-Lab"),
                Arguments.of("indiana university extreme! lab software license 1.1.1", "Indiana-University-Extreme-Lab"),
                Arguments.of("indiana university extreme! lab software license, vesion 1.1.1", "Indiana-University-Extreme-Lab"),
                Arguments.of("jquery license", "MIT:CC0-1.0"),
                Arguments.of("jsr-000107 jcache 2.9 public review - updated specification license", "Apache-2.0"),
                Arguments.of("lgpl", "LGPL-3"),
                Arguments.of("lgpl 2.1", "LGPL-2.1"),
                Arguments.of("lgpl-2.1", "LGPL-2.1"),
                Arguments.of("lgpl 3", "LGPL-3"),
                Arguments.of("lgplv2.1", "LGPL-2.1"),
                Arguments.of("lgpl, version 2.1", "LGPL-2.1"),
                Arguments.of("mit license", "MIT"),
                Arguments.of("modified bsd", "BSD"),
                Arguments.of("mozilla public license 1.1 (mpl 1.1)", "MPL-1.1"),
                Arguments.of("mozilla public license version 2.0", "MPL-2.0"),
                Arguments.of("mpl 1.1", "MPL-1.1"),
                Arguments.of("mpl 2.0 or epl 1.0", "|| ( MPL-1.1 EPL-1.0 )"),
                Arguments.of("new bsd license", "BSD"),
                Arguments.of("provided without support or warranty", "JSON"),
                Arguments.of("public domain", "public-domain"),
                Arguments.of("public domain, per creative commons cc0", "public-domain"),
                Arguments.of("public domain, sun microsoystems", "public-domain"),
                Arguments.of("revised bsd", "BSD"),
                Arguments.of("similar to apache license but with the acknowledgment clause removed", "Apache-2.0"),
                Arguments.of("spec evaluation license", "Apache-2.0"),
                Arguments.of("spec implementation license", "Apache-2.0"),
                Arguments.of("the apache license, version 2.0", "Apache-2.0"),
                Arguments.of("the apache software license, version 2.0", "Apache-2.0"),
                Arguments.of("the bsd 2-clause license", "BSD-2"),
                Arguments.of("the bsd 3-clause license", "BSD"),
                Arguments.of("the bsd license", "BSD"),
                Arguments.of("the gnu general public license (gpl), version 2, with classpath exception", "GPL-2-with-classpath-exception"),
                Arguments.of("the gnu lesser general public license, version 2.1", "LGPL-2.1"),
                Arguments.of("the go license", "BSD"),
                Arguments.of("the json license", "JSON"),
                Arguments.of("the mit license", "MIT"),
                Arguments.of("the new bsd license", "BSD"),
                Arguments.of("universal permissive license, version 1.0", "UPL-1.0"),
                Arguments.of("w3c license", "W3C")
        );
    }
}

