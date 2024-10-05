package API_CardGameSpring.services
import API_CardGameSpring.models.BotAction
import API_CardGameSpring.models.Card
import API_CardGameSpring.models.PlayGameOutput
import API_CardGameSpring.models.PlayInput
import API_CardGameSpring.models.Player

class PlayerService {

    private Player player

    Card throwCard(PlayInput input, Integer currentRound) {
        Card playerCardPlayed = player.cards[input.index]
        player.cards.remove(input.index)
        player.cardsPlayed[currentRound.toString()] = player.cardsPlayed[currentRound.toString()] + playerCardPlayed
        player.attackPoints = player.attackPoints + playerCardPlayed.attack
        return playerCardPlayed
    }

    PlayGameOutput playerMoveAndBotTurnPassed(Card playerCardPlayed, List<BotAction> botActions, Integer currentRound) {
        String gameResult = player.name + ": jogou a carta " + playerCardPlayed.name + ". Bot: passou a vez." +
                " Round atual = " + currentRound
        PlayGameOutput playOutput = new PlayGameOutput(playerCardPlayed: playerCardPlayed,
                botActions: botActions, gameResult: gameResult)
        return playOutput
    }

    PlayGameOutput playerMoveAndBotMove(Card playerCardPlayed, List<BotAction> botActions, Integer currentRound) {
        String gameResult = player.name + ": jogou a carta " + playerCardPlayed.name + ". Bot: jogou a carta " +
                botActions.botCardPlayed.name + ". Round atual = " + currentRound
        PlayGameOutput playOutput = new PlayGameOutput(playerCardPlayed: playerCardPlayed,
                botActions: botActions, gameResult: gameResult)
        return playOutput
    }

    PlayGameOutput handlePlayerTurn(PlayInput input, List<BotAction> botActions, Integer currentRound) {
        if (botService.shouldBotPlay()) {
            bot.passTurn = false
            BotAction botAction = playBot()
            botActions.add(botAction)
            return playerMoveAndBotMove(throwCard(input, currentRound), botActions, currentRound)
        } else {
            bot.passTurn = true
            if (currentRound > 3) {
                setWinner()
            }
            return playerMoveAndBotTurnPassed(throwCard(input, currentRound), botActions, currentRound)
        }
    }

    boolean checkCardIdIsValid(PlayInput input, Player player) {
        input.index = player.cards.findIndexOf { it.id == input.cardId }
        if (input.index < 0) {
            return false
        }
        return true
    }


}
