package model;

public class Role {
    private String title;
    private int level;
    private String quote;
    private Boolean taken = false;
    private int x;
    private int y;
    private int h;
    private int w;

    // Set all attributes, assume values passed in from XML Parser
    public Role(String title, int level, String quote, int x, int y, int w, int h) {
        this.title = title;
        this.level = level;
        this.quote = quote;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    // Set taken to true, makes role unavailable
    public void takeRole() {
        this.taken = true;
    }

    public void setTaken(Boolean taken) {
        this.taken = taken;
    }

    public String getTitle() {
        return title;
    }

    public int getLevel() {
        return level;
    }

    public String getQuote() {
        return quote;
    }

    // Returns true if role is taken, else false
    public Boolean isTaken() {
        return taken;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }
}
