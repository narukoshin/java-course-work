package com.narukoshin.crawler;

/**
 * @description Class for managing the table.
 * */
public class TableCollection {
    /**
     * id that will be increased all the time.
     * */
    private int id;

    /**
     * URL where the .git folder was found.
     * */
    private String url;

    /**
     * Construction method
     *
     * @param id    id of the record
     * @param url   URL of the found website
     * */
    public TableCollection(int id, String url) {
        this.url    = url;
        this.id     = id;
    }
    /**
     * Changing the id to the existing record.
     * */
    public void setId(int id) { this.id = id; }

    /**
     * Changing the url to the existing record.
     * */
    public void setUrl(String url) { this.url = url; }

    /**
     * Getting the URL from the table.
     * */
    public String getUrl(){ return url; }

    /**
     * Getting the ID from the table.
     * */
    public int getId(){ return id; }

}
