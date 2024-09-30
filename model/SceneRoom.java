package model;

import java.util.List;
import java.util.HashSet;

public class SceneRoom extends Room {
    private Boolean isActive = true;
    private int shots;
    private int shotCounter;
    private SceneCard scene;
    private List<Role> offCardRoles;
    private List<ShotCounter> shotCounters;

    public SceneRoom(String roomName, int shots, HashSet<String> adjacentRooms, List<Role> offCardRoles,
                    List<ShotCounter> shotCounters, int x, int y, int w, int h) {
        super(roomName, adjacentRooms, x, y, w, h);
        super.isSceneRoom = true;
        this.shotCounters = shotCounters;
        this.offCardRoles = offCardRoles;
        this.shotCounter = shots;
        this.shots = shots;
    }

    public void initShotCounter(int shots) {
        this.shots = shots;
        this.shotCounter = shots;
    }

    public int getShots() {
        return this.shots;
    }

    public void resetShotCounter() {
        this.shotCounter = this.shots;
    }

    public int decrementShotCounter() {
        return this.shotCounter--;
    }

    public void toggleActive() {
        this.isActive = !this.isActive;
    }

    public void setActive () {
        this.isActive = true;
    }

    public void setSceneCard(SceneCard scene) {
        this.scene = scene;
    }

    public void setOffCardRoles(List<Role> roles) {
        this.offCardRoles = roles;
    }

    public Boolean isActive() {
        return this.isActive;
    }

    public int getShotCounter() {
        return this.shotCounter;
    }

    public SceneCard getScene() {
        return this.scene;
    }

    public List<Role> getOffCardRoles() {
        return this.offCardRoles;
    }

    public List<ShotCounter> getShotCounters() {
        return this.shotCounters;
    }
}
