package API_CardGameSpring.services

import API_CardGameSpring.models.Bot
import API_CardGameSpring.models.BotAction
import API_CardGameSpring.models.Card
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class BotService {

    private Bot bot
    private Random random
    private CardService cardService

    @Autowired
    BotService(Random random, CardService cardService) {
        this.bot = new Bot()
        this.random = random
        this.cardService = cardService
    }

    List<Card> getCards() {
        return bot.cards
    }

    Integer getLife(){
        return bot.life
    }

    Integer getAttackPoints(){
        return bot.attackPoints
    }

    void resetAttackPoints(){
        bot.attackPoints = 0
    }

    void kill(){
        bot.life = bot.life - 1
    }

    BotAction throwCard(Integer currentRound, Integer playerAttackPoints) {
        if (!shouldPassTurn(playerAttackPoints)) {
            int index = random.nextInt(bot.cards.size())
            Card botCardPlayed = bot.cards.get(index)
            bot.cards.remove(index)
            bot.cardsPlayed[currentRound.toString()] = bot.cardsPlayed[currentRound.toString()] + botCardPlayed
            bot.attackPoints = bot.attackPoints + botCardPlayed.attack
            return new BotAction(botCardPlayed: botCardPlayed, passTurn: bot.passTurn)
        } else {
            return new BotAction(passTurn: bot.passTurn)
        }
    }

    List<BotAction> handleBotTurn(Integer playerAttackPoints, Integer currentRound) {
        List<BotAction> botActions = []
        do {
            BotAction botAction = throwCard(currentRound, playerAttackPoints)
            botActions.add(botAction)
        } while (!botActions.last().passTurn)
        return botActions
    }

    private boolean shouldPassTurn(Integer playerAttackPoints) {
        if (bot.passTurn) {
            return true
        }
        if (bot.cards.isEmpty() || (bot.attackPoints > playerAttackPoints)) {
            bot.passTurn = true
            return bot.passTurn
        } else if (bot.life == 1) {
            bot.passTurn = false
            return bot.passTurn
        }
        bot.passTurn = random.nextBoolean()
        return bot.passTurn
    }

    void resetBotAttributes() {
        bot.life = 2
        bot.attackPoints = 0
        bot.cards = cardService.giveRandomCards()
        bot.cardsPlayed = ["1": [], "2": [], "3": []]
    }
}
