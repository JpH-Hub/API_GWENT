package API_CardGameSpring.models

class Character {
    Integer life
    Integer attackPoints
    List<Card> cards = []
    Map<String, List<Card>> cardsPlayed = ["1":[], "2":[], "3": []]
    Boolean passTurn

}
