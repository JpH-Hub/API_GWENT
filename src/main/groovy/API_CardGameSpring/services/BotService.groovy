package API_CardGameSpring.services

import API_CardGameSpring.models.BotAction
import API_CardGameSpring.models.Card
import API_CardGameSpring.models.Character
import API_CardGameSpring.models.Game
import API_CardGameSpring.models.Status
import org.springframework.stereotype.Service

@Service
class BotService {

    private Character bot
    private Random random
    private CardService cardService

    BotService(Random random, CardService cardService) {
        this.bot = new Character()
        this.random = random
        this.cardService = cardService
    }

    List<Card> getCards(Integer idMatch, List<Game> games) {
        for (Game game: games) {
            if (game.id == idMatch) {
                if (game.status == Status.STARTED.getCode()) {
                    return game.bot.cards
                } else {
                    throw new RuntimeException("Partida não Iniciada!")
                }
            }
        }
        throw new RuntimeException("Id de partida Inválida!")
    }

    Integer getLife() {
        return bot.life
    }

    Integer getAttackPoints() {
        return bot.attackPoints
    }

    void resetAttackPoints(Game game) {
        game.bot.attackPoints = 0
    }

    void loseLife(Game game) {
        game.bot.life = game.bot.life - 1
    }

    Character getBot() {
        return bot
    }

    Boolean resetPassTurn(Game game) {
        game.bot.passTurn = false
    }

    Boolean getPassTurn() {
        return bot.passTurn
    }


    BotAction throwCard(Game game) {
        int index = random.nextInt(game.bot.cards.size())
        Card botCardPlayed = game.bot.cards.get(index)
        game.bot.cards.remove(index)
        game.bot.cardsPlayed[game.currentRound.toString()] = game.bot.cardsPlayed[game.currentRound.toString()] + botCardPlayed
        game.bot.attackPoints = game.bot.attackPoints + botCardPlayed.attack
        return new BotAction(botCardPlayed: botCardPlayed)
    }

    BotAction handleBotTurn(BotAction botAction, Game game) {
        botAction = throwCard(game)
        game.bot.passTurn = true
        return botAction
    }

    boolean shouldPassTurn(BotAction botAction, Game game) {
        if (game.bot.passTurn) {
            return true
        }
        if (game.bot.cards.isEmpty()) {
            game.bot.passTurn = true
            botAction.passTurn = game.bot.passTurn
            return botAction.passTurn
        } else if (game.bot.life == 1) {
            game.bot.passTurn = false
            botAction.passTurn = game.bot.passTurn
            return botAction.passTurn
        }
        game.bot.passTurn = random.nextBoolean()
        botAction.passTurn = game.bot.passTurn
        return botAction.passTurn
    }

    void resetBotAttributes(Game game) {
        game.bot.life = 2
        game.bot.attackPoints = 0
        game.bot.cards = cardService.giveRandomCards()
        game.bot.cardsPlayed = ["1": [], "2": [], "3": []]
        game.bot.passTurn = false
    }
}
