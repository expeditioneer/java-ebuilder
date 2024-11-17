package org.gentoo.java.ebuilder.portage.license;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class License {

    @XmlAttribute(name = "mavenlicense")
    public String mavenLicense;

    @XmlAttribute(name = "gentoolicense")
    public String gentooLicense;

    @XmlAttribute
    public String description;

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("License [mavenLicense=");
        builder.append(mavenLicense);
        builder.append(", gentooLicense=");
        builder.append(gentooLicense);

        if (description != null) {
            builder.append(", description=");
            builder.append(description);
        }
        builder.append("]");

        return builder.toString();
    }
}
