package API_CardGameSpring.services

import API_CardGameSpring.models.BotAction
import API_CardGameSpring.models.Card
import API_CardGameSpring.models.Character
import API_CardGameSpring.models.Game
import API_CardGameSpring.models.Input.GameInput
import API_CardGameSpring.models.Input.PlayInput
import API_CardGameSpring.models.Input.StartGameInput
import API_CardGameSpring.models.Output.PlayGameOutput
import API_CardGameSpring.models.Output.ResponseOutput
import API_CardGameSpring.models.Output.StartGameOutput
import API_CardGameSpring.models.Player
import API_CardGameSpring.models.Status
import org.apache.coyote.Response
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class GameService {
    private BotService botService
    private PlayerService playerService
    private Random random
    private Integer currentRound = 0
    private List<Game> games = []

    GameService(BotService botService, PlayerService playerService, Random random) {
        this.botService = botService
        this.playerService = playerService
        this.random = random
    }

    Integer getCurrentRound() {
        return currentRound
    }

    List<Game> getGames() {
        return games
    }

    ResponseOutput registerGame(GameInput input) {
        Boolean faceOrCrownResult = null
        for (Game game : games) {
            if (game.id == input.id) {
                throw new RuntimeException("id de game ja existente")
            }
        }
        Player player = new Player()
        player.name = input.playerName
        Game newGame = new Game(input.id, player, new Character(), currentRound)
        games.add(newGame)
        return new ResponseOutput(message: "Game criado com sucesso.")
    }


    StartGameOutput startGame(StartGameInput input) {
        for (Game game : games) {
            if (game.id == input.id) {
                if (game.status.equals(Status.NOT_INITIALIZED.getCode())) {
                    initializeGame(game)
                    BotAction botAction = new BotAction()
                    boolean faceOrCrownResult = random.nextBoolean()
                    if (input.faceOrCrown != faceOrCrownResult) {
                        botAction = botService.throwCard(currentRound)
                        return new StartGameOutput(faceOrCrownResult: faceOrCrownResult, botAction: botAction)
                    }
                } else {
                    throw new RuntimeException("Partida com esse id ja foi iniciada.")
                }
            } else {
                throw new RuntimeException("Não existe uma partida registrada com esse id.")
            }
        }
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


    private void initializeGame(Game game) {
        game.currentRound = 1
        playerService.resetPlayerAttributes(game)
        botService.resetBotAttributes(game)
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


