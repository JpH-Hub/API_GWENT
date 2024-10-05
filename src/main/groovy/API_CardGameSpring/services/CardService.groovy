package API_CardGameSpring.services

import API_CardGameSpring.models.Card

class CardService {

    private Random random

    CardService(Random random) {
        this.random = random
    }

    private Map<String, Card> cards = [
            "1" : new Card(id: 1, name: "Cirilla", attack: 15, position: "MELEE", faction: "neutral"),
            "2" : new Card(id: 2, name: "Gerald", attack: 15, position: "MELEE", faction: "neutral"),
            "3" : new Card(id: 3, name: "Triss", attack: 7, position: "MELEE", faction: "neutral"),
            "4" : new Card(id: 4, name: "Vernon", attack: 10, position: "MELEE", faction: "Northern Realms"),
            "5" : new Card(id: 5, name: "Imlerith", attack: 10, position: "MELEE", faction: "Monsters"),
            "6" : new Card(id: 6, name: "Phillipa", attack: 10, position: "RANGED", faction: "Northern Realms"),
            "7" : new Card(id: 7, name: "Yennefer", attack: 7, position: "RANGED", faction: "neutral"),
            "8" : new Card(id: 8, name: "Milva", attack: 10, position: "RANGED", faction: "Scoia'tael"),
            "9" : new Card(id: 9, name: "Eithné", attack: 10, position: "RANGED", faction: "Scoia'tael"),
            "10": new Card(id: 10, name: "Iorveth", attack: 10, position: "RANGED", faction: "Scoia'tael"),
            "11": new Card(id: 11, name: "Catapult", attack: 8, position: "SIEGE", faction: "Northern Realms"),
            "12": new Card(id: 12, name: "Thaler", attack: 1, position: "SIEGE", faction: "Northern Realms"),
            "13": new Card(id: 13, name: "Fire Elemental", attack: 6, position: "SIEGE", faction: "Monsters"),
            "14": new Card(id: 14, name: "Morvran Voorhis", attack: 10, position: "SIEGE", faction: "NilfGaard"),
            "15": new Card(id: 15, name: "Gaunter O'Dim", attack: 2, position: "SIEGE", faction: "Neutral")
    ]

    Map<String, Card> getCards() {
        return cards
    }

    void addCards(Card card) {
        cards.put(card.id.toString(), card)
    }

    List<Card> giveRandomCards() {
        List<Card> randomCards = []
        for (int i = 0; i < 5; i++) {
            int id
            id = random.nextInt(cards.size()) + 1
            randomCards.add(cards.get(id.toString()))
        }
        return randomCards
    }

}
