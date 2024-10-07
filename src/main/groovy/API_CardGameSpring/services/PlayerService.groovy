package API_CardGameSpring.services
import API_CardGameSpring.models.Card
import API_CardGameSpring.models.Input.PlayInput
import API_CardGameSpring.models.Player
import API_CardGameSpring.models.Input.StartGameInput
import org.springframework.stereotype.Service

@Service
class PlayerService {

    private Player player
    private CardService cardService

    PlayerService(CardService cardService) {
        this.player = new Player()
        this.cardService = cardService
    }

    List<Card> getCards() {
        return player.cards
    }

    String getName() {
        return player.name
    }

    Integer getLife(){
        return player.life
    }

    void resetAttackPoints(){
        player.attackPoints = 0
    }

    void loseLife(){
        player.life = player.life - 1
    }

    Card throwCard(PlayInput input, Integer currentRound) {
        Card playerCardPlayed = player.cards[input.index]
        player.cards.remove(input.index)
        player.cardsPlayed[currentRound.toString()] = player.cardsPlayed[currentRound.toString()] + playerCardPlayed
        player.attackPoints = player.attackPoints + playerCardPlayed.attack
        return playerCardPlayed
    }

    boolean checkCardIdIsValid(PlayInput input) {
        input.index = player.cards.findIndexOf { it.id == input.cardId }
        if (input.index < 0) {
            return false
        }
        return true
    }

    boolean shouldPassTurn(PlayInput input) {
        if (player.cards.isEmpty() || input.passTurn) {
            return true
        }
        return false
    }

    Integer getAttackPoints() {
        return player.attackPoints
    }

    void resetPlayerAttributes(StartGameInput input) {
        player.life = 2
        player.attackPoints = 0
        player.cards = cardService.giveRandomCards()
        player.name = input.player.name
    }

}
