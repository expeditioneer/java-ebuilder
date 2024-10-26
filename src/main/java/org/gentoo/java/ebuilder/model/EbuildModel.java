package org.gentoo.java.ebuilder.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.util.List;
import java.util.Objects;

@Entity
public class EbuildModel extends PanacheEntity {
    public String category;
    public String pn;
    public String pvr;
    public String slot;
    public String useFlag;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    public List<MavenCoordinates> mavenCoordinates;

    public List<String> javaEclass;

    public EbuildModel() {
    }

    public String getPV() {
        return pvr.split("-")[0];
    }

    public EbuildModel(String category, String pn, String pvr, String slot, String useFlag, MavenCoordinates mavenCoordinates, List<String> javaEclass) {
        this.category = category;
        this.pn = pn;
        this.pvr = pvr;
        this.slot = slot;
        this.useFlag = useFlag;
        this.mavenCoordinates = List.of(mavenCoordinates);
        this.javaEclass = javaEclass;
    }

    public EbuildModel(String category, String pn, String pvr, String slot, String useFlag, List<MavenCoordinates> mavenCoordinates, List<String> javaEclass) {
        this.category = category;
        this.pn = pn;
        this.pvr = pvr;
        this.slot = slot;
        this.useFlag = useFlag;
        this.mavenCoordinates = mavenCoordinates;
        this.javaEclass = javaEclass;
    }

    @Override
    public String toString() {
        return "EbuildModel{" +
                "category='" + category + '\'' +
                ", pn='" + pn + '\'' +
                ", pvr='" + pvr + '\'' +
                ", slot='" + slot + '\'' +
                ", useFlag='" + useFlag + '\'' +
                ", mavenCoordinates=" + mavenCoordinates +
                '}';
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EbuildModel that)) return false;

        return Objects.equals(category, that.category) &&
                Objects.equals(pn, that.pn) &&
                Objects.equals(pvr, that.pvr) &&
                Objects.equals(slot, that.slot) &&
                Objects.equals(useFlag, that.useFlag) &&
                Objects.equals(mavenCoordinates, that.mavenCoordinates);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(category);
        result = 31 * result + Objects.hashCode(pn);
        result = 31 * result + Objects.hashCode(pvr);
        result = 31 * result + Objects.hashCode(slot);
        result = 31 * result + Objects.hashCode(useFlag);
        result = 31 * result + Objects.hashCode(mavenCoordinates);
        return result;
    }
}
