package model;

import java.util.List;

public class SceneCard {
    private String name;
    private String img;
    private int budget;
    private int number;
    private String description;
    private List<Role> onCardRoles;

    // Set all attributes, assume values passed in from XML Parser
    public SceneCard(String name, int budget, String description, int number, String img, List<Role> onCardRoles) {
        this.name = name;
        this.budget = budget;
        this.description = description;
        this.number = number;
        this.img = img;
        this.onCardRoles = onCardRoles;
    }

    public String getName() {
        return name;
    }

    public int getBudget() {
        return budget;
    }

    public String getDescription() {
        return description;
    }

    public int getNumber() {
        return number;
    }

    public String getImgName() {
        return img;
    }

    public List<Role> getOnCardRoles() {
        return onCardRoles;
    }

}
