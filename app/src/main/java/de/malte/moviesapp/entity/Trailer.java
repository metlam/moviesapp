package de.malte.moviesapp.entity;

/**
 * Trailer object
 */
public class Trailer {

    private String id;
    private String key;
    private String name;
    private String title;
    private String type;

    public Trailer(String id, String key, String name, String title, String type) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.title = title;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
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
}
