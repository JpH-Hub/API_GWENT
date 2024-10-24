package API_CardGameSpring.services
import API_CardGameSpring.models.Card
import API_CardGameSpring.models.Game
import API_CardGameSpring.models.Input.PlayInput
import API_CardGameSpring.models.Input.PlayPVPInput
import API_CardGameSpring.models.Player
import API_CardGameSpring.models.Status
import org.springframework.stereotype.Service

@Service
class PlayerService {

    private Player player
    private CardService cardService

    PlayerService(CardService cardService) {
        this.player = new Player()
        this.cardService = cardService
    }

    List<Card> getCards(Integer idMatch, List<Game> games) {
        for (Game game: games) {
            if (game.id == idMatch) {
                if (game.status == Status.STARTED.getCode()) {
                    return game.player1.cards
                } else {
                    throw new RuntimeException("Partida não Iniciada!")
                }
            }
        }
        throw new RuntimeException("Id de partida Inválida!")
    }

    String getName() {
        return player.name
    }

    Integer getLife(){
        return player.life
    }

    void resetAttackPoints(Game game){
        game.player1.attackPoints = 0
    }

    void loseLife(Game game){
        game.player1.life = game.player1.life - 1
    }

    Card throwCard(PlayInput input, Game game) {
        Card playerCardPlayed = game.player1.cards[input.index]
        game.player1.cards.remove(input.index)
        game.player1.cardsPlayed[game.currentRound.toString()] = game.player1.cardsPlayed[game.currentRound.toString()] + playerCardPlayed
        game.player1.attackPoints = game.player1.attackPoints + playerCardPlayed.attack
        return playerCardPlayed
    }

    boolean checkCardIdIsValid(PlayInput input, Game game) {
        input.index = game.player1.cards.findIndexOf { it.id == input.cardId }
        if (input.index < 0) {
            return false
        }
        return true
    }

    boolean shouldPassTurn(PlayInput input, Game game) {
        if (game.player1.cards.isEmpty() || input.passTurn) {
            return true
        }
        return false
    }

    Integer getAttackPoints() {
        return player.attackPoints
    }

    void resetPlayerAttributes(Game game) {
        game.player1.life = 2
        game.player1.attackPoints = 0
        game.player1.cardsPlayed = ["1": [], "2": [], "3": []]
        game.player1.cards = cardService.giveRandomCards()
    }

    void resetPlayerAttributesPVP(Game game) {
        game.player1.life = 2
        game.player1.attackPoints = 0
        game.player1.cardsPlayed = ["1": [], "2": [], "3": []]
        game.player1.cards = cardService.giveRandomCards()
        game.player2.life = 2
        game.player2.attackPoints = 0
        game.player2.cardsPlayed = ["1": [], "2": [], "3": []]
        game.player2.cards = cardService.giveRandomCards()
    }

}
