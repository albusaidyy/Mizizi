package com.example.mizizi.models;

public class ModelAlert {
    String aId, aTitle, aDescr, aImage, aTime, uid, uDp, uEmail, uName, uLat, uLong;

    public ModelAlert() {
    }

    public ModelAlert(String aId, String aTitle, String aDescr, String aImage, String aTime, String uid, String uDp, String uEmail, String uName, String uLat, String uLong) {
        this.aId = aId;
        this.aTitle = aTitle;
        this.aDescr = aDescr;
        this.aImage = aImage;
        this.aTime = aTime;
        this.uid = uid;
        this.uDp = uDp;
        this.uEmail = uEmail;
        this.uName = uName;
        this.uLat = uLat;
        this.uLong = uLong;
    }

    public String getaId() {
        return aId;
    }

    public void setaId(String aId) {
        this.aId = aId;
    }

    public String getaTitle() {
        return aTitle;
    }

    public void setaTitle(String aTitle) {
        this.aTitle = aTitle;
    }

    public String getaDescr() {
        return aDescr;
    }

    public void setaDescr(String aDescr) {
        this.aDescr = aDescr;
    }

    public String getaImage() {
        return aImage;
    }

    public void setaImage(String aImage) {
        this.aImage = aImage;
    }

    public String getaTime() {
        return aTime;
    }

    public void setaTime(String aTime) {
        this.aTime = aTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuLat() {
        return uLat;
    }

    public void setuLat(String uLat) {
        this.uLat = uLat;
    }

    public String getuLong() {
        return uLong;
    }

    public void setuLong(String uLong) {
        this.uLong = uLong;
    }
}
