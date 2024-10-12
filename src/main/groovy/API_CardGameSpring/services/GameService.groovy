package API_CardGameSpring.services

import API_CardGameSpring.models.BotAction
import API_CardGameSpring.models.Card
import API_CardGameSpring.models.Input.PlayInput
import API_CardGameSpring.models.Input.StartGameInput
import API_CardGameSpring.models.Output.PlayGameOutput
import API_CardGameSpring.models.Output.StartGameOutput
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class GameService {
    private BotService botService
    private PlayerService playerService
    private Random random
    private Integer currentRound = 0

    GameService(BotService botService, PlayerService playerService, Random random) {
        this.botService = botService
        this.playerService = playerService
        this.random = random
    }

    Integer getCurrentRound(){
        return currentRound
    }
    StartGameOutput startGame(StartGameInput input) {
        initializeGame(input)
        BotAction botAction = new BotAction()
        boolean faceOrCrownResult = random.nextBoolean()
        if (input.faceOrCrown != faceOrCrownResult) {
            botAction = botService.throwCard(currentRound)
            StartGameOutput output = new StartGameOutput(faceOrCrownResult: faceOrCrownResult, botAction: botAction)
            return output
        }
        return new StartGameOutput(faceOrCrownResult: faceOrCrownResult, botAction: botAction)
    }



    PlayGameOutput playTheGame(PlayInput input) {
        BotAction botAction = new BotAction()
        Card playedCard = null
        String gameResult = ""
        if (playerService.getLife() <= 0 || botService.getLife() <= 0) {
            throw new RuntimeException("O jogo ja foi finalizado, inicie um novo jogo")
        }
        if (!playerService.shouldPassTurn(input)) {
            if (playerService.checkCardIdIsValid(input)) {
                playedCard = playerService.throwCard(input, currentRound)
                gameResult = "${playerService.getName()}: jogou a carta ${playedCard.name} "
                if (botService.shouldPassTurn(botAction)) {
                    gameResult = "${gameResult}. Bot: passou a vez"
                } else {
                    botAction = botService.throwCard(currentRound)
                    gameResult = "${gameResult}. Bot: jogou a carta ${botAction.botCardPlayed.name}"
                }
            } else {
                throw new RuntimeException("A carta que tentaste jogar é inválida")
            }
        } else {
            if (botService.shouldPassTurn(botAction)) {
                gameResult = "${playerService.getName()}: passou a vez. Bot: passou a vez."
            } else {
                botAction = botService.handleBotTurn(currentRound, botAction)
                gameResult = "${playerService.getName()}: passou a vez. Bot: jogou a carta ${botAction.botCardPlayed.name}"
            }
        }
        if (botService.getPassTurn() && playerService.shouldPassTurn(input) && currentRound <= 3) {
            startANewRound()
            botService.resetPassTurn()
        }
        gameResult = "${gameResult}. Round atual = ${currentRound}"
        if (playerService.life <= 0 || botService.life <= 0 || currentRound > 3) {
            gameResult = getWinner()
        }
        return new PlayGameOutput(botAction: botAction, playerCardPlayed: playedCard, gameResult: gameResult)
    }


    private void initializeGame(StartGameInput input) {
        currentRound = 1
        playerService.resetPlayerAttributes(input)
        botService.resetBotAttributes()
    }

    private void startANewRound() {
        currentRound++
        if (playerService.getAttackPoints() > botService.getAttackPoints()) {
            botService.loseLife()
        } else if (playerService.getAttackPoints() < botService.getAttackPoints()) {
            playerService.loseLife()
        } else {
            botService.loseLife()
            playerService.loseLife()
        }
        playerService.resetAttackPoints()
        botService.resetAttackPoints()
    }

    private String getWinner() {
        if (playerService.getLife() <= 0 && botService.getLife() <= 0) {
            return "Empatou o Jogo!"
        } else if (botService.getLife() <= 0) {
            return playerService.getName() + " ganhou o Jogo!"
        } else {
            return "Bot ganhou o Jogo!"
        }
    }

}


