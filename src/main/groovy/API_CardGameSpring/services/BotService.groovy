package API_CardGameSpring.services

import API_CardGameSpring.models.Bot
import API_CardGameSpring.models.BotAction
import API_CardGameSpring.models.Card
import API_CardGameSpring.models.PlayGameOutput
import API_CardGameSpring.models.Player


class BotService {

    private Bot bot
    private Random random

    BotService(Random random) {
        this.random = random
        this.bot = new Bot()
    }

    BotAction playBot(Integer currentRound) {
        int index = random.nextInt(bot.cards.size())
        Card botCardPlayed = bot.cards.get(index)
        bot.cards.remove(index)
        bot.cardsPlayed[currentRound.toString()] = bot.cardsPlayed[currentRound.toString()] + botCardPlayed
        bot.attackPoints = bot.attackPoints + botCardPlayed.attack
        return new BotAction(botCardPlayed: botCardPlayed)
    }


    PlayGameOutput handleBotTurn(List<BotAction> botActions, Player player, Integer currentRound) {
        while (shouldBotPlay()) {
            if (bot.attackPoints > player.attackPoints) {
                break
            } else {
                BotAction botAction = playBot(currentRound)
                botActions.add(botAction)
            }
        }
        startANewRound()
        if (currentRound > 3) {
            setWinner()
        } else if (player.passTurn && botActions.size() > 0) {
            String gameResult = player.name + ": passou a vez. Bot: jogou a carta " +
                    botActions.botCardPlayed.name + " e depois passou a vez. novo round =" + currentRound
            PlayGameOutput playOutput = new PlayGameOutput(botActions: botActions, gameResult: gameResult)
            return playOutput
        } else {
            String gameResult = player.name + ": passou a vez. Bot: passou a vez. Novo round atual = " + currentRound
            PlayGameOutput playOutput = new PlayGameOutput(botActions: botActions, gameResult: gameResult)
            return playOutput
        }
    }

    boolean shouldBotPlay() {
        if (bot.life == 1 && bot.cards.isEmpty()) {
            return false
        } else if (bot.cards.isEmpty()) {
            return false
        } else if (bot.life == 1) {
            return true
        } else if (bot.passTurn) {
            return false
        }
        return random.nextBoolean()
    }

}
