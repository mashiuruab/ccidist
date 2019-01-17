package com.cefalo.cci.model;

public class Content {
    private int height;
    private int width;
    private String os;
    private String osVersion;
    private String device;
    private String reader;

    public Content() {
    }

    public Content(int width, int height, String os, String osVersion, String device, String reader) {
        this.width = width;
        this.height = height;
        this.os = os;
        this.osVersion = osVersion;
        this.device = device;
        this.reader = reader;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getReader() {
        return reader;
    }

    public void setReader(String reader) {
        this.reader = reader;
    }

    @Override
    public String toString() {
        return "Content ["
                + "height=" + height
                + ", width=" + width
                + ", os=" + os
                + ", osVersion=" + osVersion
                + ", device=" + device
                + ", reader=" + reader
                + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((device == null) ? 0 : device.hashCode());
        result = prime * result + height;
        result = prime * result + ((os == null) ? 0 : os.hashCode());
        result = prime * result + ((osVersion == null) ? 0 : osVersion.hashCode());
        result = prime * result + ((reader == null) ? 0 : reader.hashCode());
        result = prime * result + width;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Content other = (Content) obj;
        if (device == null) {
            if (other.device != null) {
                return false;
            }
        } else if (!device.equals(other.device)) {
            return false;
        }
        if (height != other.height) {
            return false;
        }
        if (os == null) {
            if (other.os != null) {
                return false;
            }
        } else if (!os.equals(other.os)) {
            return false;
        }
        if (osVersion == null) {
            if (other.osVersion != null) {
                return false;
            }
        } else if (!osVersion.equals(other.osVersion)) {
            return false;
        }
        if (reader == null) {
            if (other.reader != null) {
                return false;
            }
        } else if (!reader.equals(other.reader)) {
            return false;
        }
        if (width != other.width) {
            return false;
        }
        return true;
    }

}
