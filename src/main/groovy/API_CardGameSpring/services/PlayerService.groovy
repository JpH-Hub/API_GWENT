package API_CardGameSpring.services

import API_CardGameSpring.models.BotAction
import API_CardGameSpring.models.Card
import API_CardGameSpring.models.PlayGameOutput
import API_CardGameSpring.models.PlayInput
import API_CardGameSpring.models.Player
import org.springframework.stereotype.Service

@Service
class PlayerService {

    LogicGameService logicGameService = new LogicGameService()
    BotService botService = new BotService()
    Player player = new Player()

    Card throwCard(PlayInput input) {
        Card playerCardPlayed = player.cards[input.index]
        player.cards.remove(input.index)
        player.cardsPlayed[logicGameService.currentRound.toString()] = player.cardsPlayed[logicGameService.currentRound.toString()] + playerCardPlayed
        player.attackPoints = player.attackPoints + playerCardPlayed.attack
        return playerCardPlayed
    }

    PlayGameOutput playerMoveAndBotTurnPassed(Card playerCardPlayed, List<BotAction> botActions) {
        String gameResult = player.name + ": jogou a carta " + playerCardPlayed.name + ". Bot: passou a vez." +
                " Round atual = " + logicGameService.currentRound
        PlayGameOutput playOutput = new PlayGameOutput(playerCardPlayed: playerCardPlayed,
                botActions: botActions, gameResult: gameResult)
        return playOutput
    }

    PlayGameOutput playerMoveAndBotMove(Card playerCardPlayed, List<BotAction> botActions) {
        String gameResult = player.name + ": jogou a carta " + playerCardPlayed.name + ". Bot: jogou a carta " +
                botActions.botCardPlayed.name + ". Round atual = " + logicGameService.currentRound
        PlayGameOutput playOutput = new PlayGameOutput(playerCardPlayed: playerCardPlayed,
                botActions: botActions, gameResult: gameResult)
        return playOutput
    }

    PlayGameOutput handlePlayerTurn(PlayInput input, List<BotAction> botActions) {
        if (botService.shouldBotPlay()) {
            botService.bot.passTurn = false
            BotAction botAction = botService.playBot()
            botActions.add(botAction)
            return playerMoveAndBotMove(throwCard(input), botActions)
        } else {
            botService.bot.passTurn = true
            logicGameService.startANewRound()
            if (logicGameService.currentRound > 3) {
                logicGameService.setWinner()
            }
            return playerMoveAndBotTurnPassed(throwCard(input), botActions)
        }
    }

    boolean cardIdInvalid(PlayInput input) {
        input.index = player.cards.findIndexOf { it.id == input.cardId }
        if (input.index < 0) {
            return true
        }
        return false
    }
}
