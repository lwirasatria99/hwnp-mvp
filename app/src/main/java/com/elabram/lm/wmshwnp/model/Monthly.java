package com.elabram.lm.wmshwnp.model;

public class Monthly {

    private String attIdToken;
    private String attDate;
    private String attDay;
    private String timeIn;
    private String timeOut;
    private String projectName;
    private String networkNo;
    private String description;
    private String locationFirst;
    private String locationLast;
    private String totalWorkingHour;
    private String timezone1;
    private String timezone2;

    public Monthly() {
    }

    public String getTimezone1() {
        return timezone1;
    }

    public void setTimezone1(String timezone1) {
        this.timezone1 = timezone1;
    }

    public String getTimezone2() {
        return timezone2;
    }

    public void setTimezone2(String timezone2) {
        this.timezone2 = timezone2;
    }

    public String getAttIdToken() {
        return attIdToken;
    }

    public void setAttIdToken(String attIdToken) {
        this.attIdToken = attIdToken;
    }

    public String getAttDate() {
        return attDate;
    }

    public void setAttDate(String attDate) {
        this.attDate = attDate;
    }

    public String getAttDay() {
        return attDay;
    }

    public void setAttDay(String attDay) {
        this.attDay = attDay;
    }

    public String getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(String timeIn) {
        this.timeIn = timeIn;
    }

    public String getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(String timeOut) {
        this.timeOut = timeOut;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getNetworkNo() {
        return networkNo;
    }

    public void setNetworkNo(String networkNo) {
        this.networkNo = networkNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocationFirst() {
        return locationFirst;
    }

    public void setLocationFirst(String locationFirst) {
        this.locationFirst = locationFirst;
    }

    public String getLocationLast() {
        return locationLast;
    }

    public void setLocationLast(String locationLast) {
        this.locationLast = locationLast;
    }

    public String getTotalWorkingHour() {
        return totalWorkingHour;
    }

    public void setTotalWorkingHour(String totalWorkingHour) {
        this.totalWorkingHour = totalWorkingHour;
    }
}
