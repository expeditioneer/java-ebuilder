package org.gentoo.java.ebuilder.portage.license;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "licenses")
public class Licenses {

    @XmlElement(name = "license", type = License.class)
    private List<License> licenses = new ArrayList<>();

    public List<License> getLicenses() {
        return licenses;
    }
}
