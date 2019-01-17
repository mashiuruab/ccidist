package com.cefalo.cci.model;

import java.util.List;

public class Metadata {
    private String creator;
    private String title;
    private String device;
    private String coverImageLink;
    private String epubStatus;
    private List<SectionImage> sectionCoverImages;

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getCoverImageLink() {
        return coverImageLink;
    }

    public void setCoverImageLink(String coverImageLink) {
        this.coverImageLink = coverImageLink;
    }

    public String getEpubStatus() {
        return epubStatus;
    }

    public void setEpubStatus(String epubStatus) {
        this.epubStatus = epubStatus;
    }

    public List<SectionImage> getSectionCoverImages() {
        return sectionCoverImages;
    }

    public void setSectionCoverImages(List<SectionImage> sectionCoverImages) {
        this.sectionCoverImages = sectionCoverImages;
    }
}
