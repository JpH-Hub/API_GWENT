package API_CardGameSpring.services

import API_CardGameSpring.models.BotAction
import API_CardGameSpring.models.Card
import API_CardGameSpring.models.Character
import API_CardGameSpring.models.Game
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

    List<Card> getCards() {
        return bot.cards
    }

    Integer getLife() {
        return bot.life
    }

    Integer getAttackPoints() {
        return bot.attackPoints
    }

    void resetAttackPoints() {
        bot.attackPoints = 0
    }

    void loseLife() {
        bot.life = bot.life - 1
    }

    Character getBot() {
        return bot
    }

    Boolean resetPassTurn() {
        bot.passTurn = false
    }

    Boolean getPassTurn() {
        return bot.passTurn
    }


    BotAction throwCard(Integer currentRound) {
        int index = random.nextInt(bot.cards.size())
        Card botCardPlayed = bot.cards.get(index)
        bot.cards.remove(index)
        bot.cardsPlayed[currentRound.toString()] = bot.cardsPlayed[currentRound.toString()] + botCardPlayed
        bot.attackPoints = bot.attackPoints + botCardPlayed.attack
        return new BotAction(botCardPlayed: botCardPlayed)
    }

    BotAction handleBotTurn(Integer currentRound, BotAction botAction) {
        botAction = throwCard(currentRound)
        bot.passTurn = true
        return botAction
    }

    boolean shouldPassTurn(BotAction botAction) {
        if (bot.passTurn) {
            return true
        }
        if (bot.cards.isEmpty()) {
            bot.passTurn = true
            botAction.passTurn = bot.passTurn
            return botAction.passTurn
        } else if (bot.life == 1) {
            bot.passTurn = false
            botAction.passTurn = bot.passTurn
            return botAction.passTurn
        }
        bot.passTurn = random.nextBoolean()
        botAction.passTurn = bot.passTurn
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
