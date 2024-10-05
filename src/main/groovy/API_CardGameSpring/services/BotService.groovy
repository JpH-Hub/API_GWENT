package API_CardGameSpring.services

import API_CardGameSpring.models.Bot
import API_CardGameSpring.models.BotAction
import API_CardGameSpring.models.Card

class BotService {

    private Bot bot
    private Random random
    private CardService cardService

    BotService(Random random, CardService cardService) {
        this.bot = new Bot()
        this.random = random
        this.cardService = cardService
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
        if (bot.cards.isEmpty() || (bot.attackPoints > playerAttackPoints)) {
            bot.passTurn = true
        } else if (bot.life == 1) {
            bot.passTurn = false
        }
        bot.passTurn = random.nextBoolean()
        return bot.passTurn
    }

}
