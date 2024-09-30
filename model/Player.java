package model;

public class Player {
    private String name;
    private String roomName;
    private Role role = null;
    private int practiceChips = 0;
    private int score = 0;
    private int rank = 1;
    private int dollars = 0;
    private int credits = 0;
    private boolean workingOffCard;

    // init player at the start of the game, name from users and at the trailers
    public Player(String name, String roomName) {
        this.name = name;
        this.roomName = roomName;
    }

    // updates the score of the active player
    public void updateScore() {
        this.score = this.dollars + this.credits + (5 * this.rank);
    }

    // upgrades the player's rank, with prices in credits or dollars
    public Boolean upgrade(int rank, boolean withDollars) {
        // get price needed to upgrade
        int price;
        switch (rank) {
            case 2:
                price = withDollars ? 4 : 5;
                break;
            case 3:
                price = withDollars ? 10 : 10;
                break;
            case 4:
                price = withDollars ? 18 : 15;
                break;
            case 5:
                price = withDollars ? 28 : 20;
                break;
            case 6:
                price = withDollars ? 40 : 25;
                break;
            default:
                System.err.println("Invalid rank passed.");
                return false;
        }

        boolean canPay = withDollars ? (this.dollars >= price) : (this.credits >= price);
        boolean inCastingOffice = this.roomName.equals("office");

        if (inCastingOffice && canPay) {
            this.rank = rank;
            this.dollars -= withDollars ? price : 0;
            this.credits -= withDollars ? 0 : price;
        }

        return canPay;
    }

    // returns if player can take a role? ask about removing
    public Boolean takeRole(Role role) {
        if (role.isTaken()) {
            return false;
        } else {
            role.takeRole();
            this.role = role;
        }
        return true;
    }

    public Boolean rehearse() {
        this.practiceChips++;
        return true;
    }

    // moves player to new room, toRoom
    public void move(String toRoom) {
        this.roomName = toRoom;
    }

    public String getName() {
        return this.name;
    }

    public String getRoomName() {
        return this.roomName;
    }

    public Role getRole() {
        return this.role;
    }

    public int getScore() {
        return this.score;
    }

    public int getPracticeChips() {
        return this.practiceChips;
    }

    public int getRank() {
        return this.rank;
    }

    public int getDollars() {
        return this.dollars;
    }

    public int getCredits() {
        return this.credits;
    }

    public boolean hasRole() {
        return this.role != null;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public void setPChips(int practiceChips) {
        this.practiceChips = practiceChips;
    }

    // takes role and sets it to player's role
    public void setRole(Role role) {
        if (role != null) {
            role.takeRole();
        }
        this.role = role;
    }

    public void setWorkingOffCard(boolean isOffCardRole) {
        this.workingOffCard = isOffCardRole;
    }

    public boolean getWorkingOffCard() {
        return this.workingOffCard;
    }

    public void incrementRank(int val) {
        this.rank += val;
    }

    public void incrementDollars(int val) {
        this.dollars += val;
    }

    public void incrementCredits(int val) {
        this.credits += val;
    }
}
