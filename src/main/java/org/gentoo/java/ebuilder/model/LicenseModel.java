package org.gentoo.java.ebuilder.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import org.gentoo.java.ebuilder.portage.license.License;

import java.util.Optional;


@Entity(name = "Licenses")
public class LicenseModel extends PanacheEntity {

    public String mavenLicense;
    public String gentooLicense;

    public LicenseModel() {

    }

    public LicenseModel(License license) {
        this.mavenLicense = license.mavenLicense;
        this.gentooLicense = license.gentooLicense;
    }

    public static Optional<LicenseModel> findMatchingLicenseModelOptional(String mavenLicense) {
        return Optional.ofNullable(find("mavenLicense", mavenLicense.toLowerCase())
                .firstResult());
    }

    @Override
    public String toString() {
        return "LicenseModel{" +
                "mavenLicense='" + mavenLicense + '\'' +
                ", gentooLicense='" + gentooLicense + '\'' +
                '}';
    }
}
