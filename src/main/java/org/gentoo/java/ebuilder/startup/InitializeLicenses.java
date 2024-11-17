
package org.gentoo.java.ebuilder.startup;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.gentoo.java.ebuilder.model.LicenseModel;
import org.gentoo.java.ebuilder.portage.license.Licenses;

import java.io.File;
import java.nio.file.Path;

@ApplicationScoped
public class InitializeLicenses {

    File licenseFile = Path.of("src/main/resources/licenses.xml").toFile();

    Licenses licenses;

    @Startup
    void initialize() throws JAXBException {
        parseLicenses();
        importLicenses();
    }

    private void parseLicenses() throws JAXBException {
        Log.info("Parsing known and assigned licenses ...");
        JAXBContext jaxbContext = JAXBContext.newInstance(Licenses.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        licenses = (Licenses) jaxbUnmarshaller.unmarshal(licenseFile);
        Log.info("... parsing licenses done.");
    }

    @Transactional
    void importLicenses() {
        Log.info("Importing licenses ...");
        licenses.getLicenses().stream()
                .map(LicenseModel::new)
                .forEach(licenseModel -> {
                    licenseModel.persist();
                    Log.debug(licenseModel);
                });
        Log.info("... importing licenses done.");
    }
}
