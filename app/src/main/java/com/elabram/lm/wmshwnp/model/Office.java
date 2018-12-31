package com.elabram.lm.wmshwnp.model;

public class Office
{
    private String oc_site;
    private String oc_lat;
    private String oc_long;
    private String oc_radius;

    private String oc_id;

    public Office() {
    }

    public String getOc_radius() {
        return oc_radius;
    }

    public void setOc_radius(String oc_radius) {
        this.oc_radius = oc_radius;
    }

    public String getOc_site() {
        return oc_site;
    }

    public void setOc_site(String oc_site) {
        this.oc_site = oc_site;
    }

    public String getOc_lat() {
        return oc_lat;
    }

    public void setOc_lat(String oc_lat) {
        this.oc_lat = oc_lat;
    }

    public String getOc_long() {
        return oc_long;
    }

    public void setOc_long(String oc_long) {
        this.oc_long = oc_long;
    }

    public String getOc_id() {
        return oc_id;
    }

    public void setOc_id(String oc_id) {
        this.oc_id = oc_id;
    }
}
