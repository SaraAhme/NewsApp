package com.example.android.news;

/**
 * Created by alslam on 12/05/2018.
 */

public class NewsFeed {
    String title, type, date, auther, weburl,secNam;

    public NewsFeed(String title, String type, String date, String auther, String weburl,String secNam) {
        this.title = title;
        this.type = type;
        this.date = date;
        this.auther = auther;
        this.weburl = weburl;
        this.secNam=secNam;
    }

    public String getTitle() {
        return title;
    }

    public String getWeburl() {
        return weburl;
    }

    public void setWeburl(String weburl) {
        this.weburl = weburl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getsecNam() {
        return secNam;
    }

    public void setsecNam(String secNam) {
        this.secNam = secNam;
    }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getAuther() {return auther;}

    public void setAuther(String auther) {
        this.auther = auther;
    }
}
