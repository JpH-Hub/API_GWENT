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
                    BotAction botAction
                    boolean faceOrCrownResult = random.nextBoolean()
                    if (input.faceOrCrown != faceOrCrownResult) {
                        botAction = botService.throwCard(game)
                        return new StartGameOutput(faceOrCrownResult: faceOrCrownResult, botAction: botAction)
                    }
                } else {
                    throw new RuntimeException("Partida com esse id ja foi iniciada.")
                }
            }
        }
    }


    PlayGameOutput playTheGame(PlayInput input) {
        for (Game game : games) {
            if (game.id == input.id) {
                if (game.status == Status.STARTED.getCode()) {
                    BotAction botAction = new BotAction()
                    Card playedCard = null
                    String gameResult = ""
                    if (!playerService.shouldPassTurn(input, game)) {
                        if (playerService.checkCardIdIsValid(input, game)) {
                            playedCard = playerService.throwCard(input, game)
                            gameResult = "${game.player.getName()}: jogou a carta ${playedCard.name} "
                            if (botService.shouldPassTurn(botAction, game)) {
                                gameResult = "${gameResult}. Bot: passou a vez"
                            } else {
                                botAction = botService.throwCard(game)
                                gameResult = "${gameResult}. Bot: jogou a carta ${botAction.botCardPlayed.name}"
                            }
                        } else {
                            throw new RuntimeException("A carta que tentaste jogar é inválida")
                        }
                    } else {
                        if (botService.shouldPassTurn(botAction, game)) {
                            gameResult = "${game.player.getName()}: passou a vez. Bot: passou a vez."
                        } else {
                            botAction = botService.handleBotTurn(botAction, game)
                            gameResult = "${game.player.getName()}: passou a vez. Bot: jogou a carta ${botAction.botCardPlayed.name}"
                        }
                    }
                    if (game.bot.getPassTurn() && playerService.shouldPassTurn(input, game) && game.currentRound <= 3) {
                        startANewRound(game)
                        botService.resetPassTurn(game)
                    }
                    gameResult = "${gameResult}. Round atual = ${game.currentRound}"
                    if (game.player.getLife() <= 0 || game.bot.getLife() <= 0 || game.currentRound > 3) {
                        gameResult = getWinner(game)
                    }
                    return new PlayGameOutput(botAction: botAction, playerCardPlayed: playedCard, gameResult: gameResult)
                } else {
                    throw new RuntimeException("O jogo ja foi finalizado!")
                }
            }
        }
        throw new RuntimeException("Não tem game criado com esse id")
    }


    private void initializeGame(Game game) {
        game.status = Status.STARTED.getCode()
        game.currentRound = 1
        playerService.resetPlayerAttributes(game)
        botService.resetBotAttributes(game)
    }

    private void startANewRound(Game game) {
        game.currentRound++
        if (game.player.getAttackPoints() > game.bot.getAttackPoints()) {
            botService.loseLife(game)
        } else if (game.player.getAttackPoints() < game.bot.getAttackPoints()) {
            playerService.loseLife(game)
        } else {
            botService.loseLife(game)
            playerService.loseLife(game)
        }
        playerService.resetAttackPoints(game)
        botService.resetAttackPoints(game)
    }

    private String getWinner(Game game) {
        if (game.player.getLife() <= 0 && game.bot.getLife() <= 0) {
            game.status = Status.FINALIZED.getCode()
            return "Empatou o Jogo!"
        } else if (game.bot.getLife() <= 0) {
            game.status = Status.FINALIZED.getCode()
            return game.player.getName() + " ganhou o Jogo!"
        } else {
            game.status = Status.FINALIZED.getCode()
            return "Bot ganhou o Jogo!"
        }
    }

}


