package API_CardGameSpring.models

class Character {
    private Integer life
    private Integer attackPoints
    private List<Card> cards = []
    private Map<String, List<Card>> cardsPlayed = ["1":[], "2":[], "3": []]
    private Boolean passTurn

    Integer getLife() {
        return life
    }

    Integer getAttackPoints() {
        return attackPoints
    }

    Boolean getPassTurn() {
        return passTurn
    }

    List<Card> getCards() {
        return cards
    }

    Map<String, List<Card>> getCardsPlayed() {
        return cardsPlayed
    }

    void setAttackPoints(Integer attackPoints) {
        this.attackPoints = attackPoints
    }

    void setPassTurn(Boolean passTurn) {
        this.passTurn = passTurn
    }
}
