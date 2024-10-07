package API_CardGameSpring.models.Output

import API_CardGameSpring.models.Card

class StatusGameOutput {
    List<Card> botCards
    List<Card> playerCards
    String playerLife
    String botLife
    String playerAttack
    String botAttack
    int currentRound
}
