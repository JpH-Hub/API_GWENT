package API_CardGameSpring.services

import API_CardGameSpring.models.PlayGameOutput
import API_CardGameSpring.models.StartGameInput
import org.springframework.stereotype.Service

@Service
class LogicGameService {

    Random random = new Random()
    CardService cardService = new CardService()
    PlayerService playerService = new PlayerService()
    BotService botService = new BotService()
    Integer currentRound = 0

    void initializeGame(StartGameInput input) {
        playerService.player.life = 2
        botService.bot.life = 2
        currentRound = 1
        playerService.player.attackPoints = 0
        botService.bot.attackPoints = 0
        botService.bot.cards = []
        playerService.player.cards = []
        playerService.player.name = input.player.name
        botService.bot.cardsPlayed = ["1": [], "2": [], "3": []]
        cardService.giveRandomCards()
    }

    void startANewRound() {
        if (playerService.player.attackPoints > botService.bot.attackPoints) {
            botService.bot.life = botService.bot.life - 1
        } else if (botService.bot.attackPoints > playerService.player.attackPoints) {
            playerService.player.life = playerService.player.life - 1
        } else {
            botService.bot.life = botService.bot.life - 1
            playerService.player.life = playerService.player.life - 1
        }
        currentRound++
        playerService.player.attackPoints = 0
        botService.bot.attackPoints = 0
    }

    PlayGameOutput setWinner() {
        if (playerService.player.life <= 0 && botService.bot.life <= 0 ) {
            String gameResult = "Empatou o Jogo!"
            PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
            return playOutput
        } else if (botService.bot.life <= 0 ) {
            String gameResult = playerService.player.name + " ganhou o Jogo!"
            PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
            return playOutput
        } else {
            String gameResult = "Bot ganhou o Jogo!"
            PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
            return playOutput
        }
    }

}
