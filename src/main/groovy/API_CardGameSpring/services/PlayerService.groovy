package API_CardGameSpring.services
import API_CardGameSpring.models.Card
import API_CardGameSpring.models.Game
import API_CardGameSpring.models.Input.PlayInput
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
                    return game.player.cards
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
        game.player.attackPoints = 0
    }

    void loseLife(Game game){
        game.player.life = game.player.life - 1
    }

    Card throwCard(PlayInput input, Game game) {
        Card playerCardPlayed = game.player.cards[input.index]
        game.player.cards.remove(input.index)
        game.player.cardsPlayed[game.currentRound.toString()] = game.player.cardsPlayed[game.currentRound.toString()] + playerCardPlayed
        game.player.attackPoints = game.player.attackPoints + playerCardPlayed.attack
        return playerCardPlayed
    }

    boolean checkCardIdIsValid(PlayInput input, Game game) {
        input.index = game.player.cards.findIndexOf { it.id == input.cardId }
        if (input.index < 0) {
            return false
        }
        return true
    }

    boolean shouldPassTurn(PlayInput input, Game game) {
        if (game.player.cards.isEmpty() || input.passTurn) {
            return true
        }
        return false
    }

    Integer getAttackPoints() {
        return player.attackPoints
    }

    void resetPlayerAttributes(Game game) {
        game.player.life = 2
        game.player.attackPoints = 0
        game.player.cardsPlayed = ["1": [], "2": [], "3": []]
        game.player.cards = cardService.giveRandomCards()
    }

}
