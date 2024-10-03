package API_CardGameSpring.services

import API_CardGameSpring.models.Bot
import API_CardGameSpring.models.BotAction
import API_CardGameSpring.models.Card
import API_CardGameSpring.models.PlayGameOutput
import API_CardGameSpring.models.Player
import org.springframework.stereotype.Service

@Service
class BotService {

    private Player player = new Player()
    LogicGameService logicGameService = new LogicGameService()
    private Random random = new Random()
    Bot bot = new Bot()

    BotAction playBot() {
        int index = random.nextInt(bot.cards.size())
        Card botCardPlayed = bot.cards.get(index)
        bot.cards.remove(index)
        bot.cardsPlayed[logicGameService.currentRound.toString()] = bot.cardsPlayed[logicGameService.currentRound.toString()] + botCardPlayed
        bot.attackPoints = bot.attackPoints + botCardPlayed.attack
        return new BotAction(botCardPlayed: botCardPlayed)
    }


    PlayGameOutput handleBotTurn(List<BotAction> botActions) {
        while (shouldBotPlay()) {
            if (bot.attackPoints > player.attackPoints) {
                break
            } else {
                BotAction botAction = playBot()
                botActions.add(botAction)
            }
        }
        logicGameService.startANewRound()
        if (logicGameService.currentRound > 3) {
            logicGameService.setWinner()
        } else if (player.passTurn && botActions.size() > 0) {
            String gameResult = player.name + ": passou a vez. Bot: jogou a carta " +
                    botActions.botCardPlayed.name + " e depois passou a vez. novo round =" + logicGameService.currentRound
            PlayGameOutput playOutput = new PlayGameOutput(botActions: botActions, gameResult: gameResult)
            return playOutput
        } else {
            String gameResult = player.name + ": passou a vez. Bot: passou a vez. Novo round atual = " + logicGameService.currentRound
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
