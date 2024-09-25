package API_CardGameSpring.models

class Character {
    List<Card> cards = []
    Map<String, List<Card>> cardsPlayed = ["1":[], "2":[], "3": []]
    Integer life
    Integer attackPoints
    Boolean passTurn
}
