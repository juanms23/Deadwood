package view;

public class Die {
    String name, score, pChips, rank;

    public Die(String name, String score, String pChips, String rank) {
        this.name = name;
        this.score = score;
        this.pChips = pChips;
        this.rank = rank;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getPChips() {
        return pChips;
    }

    public void setPChips(String pChips) {
        this.pChips = pChips;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }
}
