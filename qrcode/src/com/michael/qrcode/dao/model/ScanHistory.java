package com.michael.qrcode.dao.model;

import java.io.Serializable;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table SCAN_HISTORY.
 */
public class ScanHistory implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Long id;
    /** Not-null value. */
    private String title;
    /** Not-null value. */
    private String content;
    private long timestamp;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public ScanHistory() {
    }

    public ScanHistory(Long id) {
        this.id = id;
    }

    public ScanHistory(Long id, String title, String content, long timestamp) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getTitle() {
        return title;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** Not-null value. */
    public String getContent() {
        return content;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "[ScanHistory]" + "id = " + id + ", " + "title = " + title + ", " + "content = " + content + ", " + "timestamp = " + timestamp + "\r\n";
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
