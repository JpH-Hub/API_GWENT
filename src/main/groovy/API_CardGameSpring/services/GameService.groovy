package API_CardGameSpring.services

import API_CardGameSpring.models.BotAction
import API_CardGameSpring.models.Card
import API_CardGameSpring.models.Character
import API_CardGameSpring.models.Format
import API_CardGameSpring.models.Game
import API_CardGameSpring.models.Input.GameInput
import API_CardGameSpring.models.Input.PlayInput
import API_CardGameSpring.models.Input.PlayPVPInput
import API_CardGameSpring.models.Input.StartGameInput
import API_CardGameSpring.models.Input.StartGamePVPInput
import API_CardGameSpring.models.Output.PlayGameOutput
import API_CardGameSpring.models.Output.PlayGamePVPOutput
import API_CardGameSpring.models.Output.ResponseOutput
import API_CardGameSpring.models.Output.StartGameOutput
import API_CardGameSpring.models.Output.StartGamePVPOutput
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
        for (Game game : games) {
            if (game.id == input.id) {
                throw new RuntimeException("id de game ja existente")
            }
        }
        if (input.formatGame == Format.PVB.getCode()) {
            Game newGame = new Game(input.id, input.formatGame, input.player1, input.player2, new Character(), currentRound)
            games.add(newGame)
        } else if (input.formatGame == Format.PVP.getCode()) {
            Game newGame = new Game(input.id, input.formatGame, input.player1, input.player2, new Character(), currentRound)
            games.add(newGame)
        } else {
            throw new RuntimeException("Formato de game inválido")
        }
        return new ResponseOutput(message: "Game criado com sucesso.")
    }


    StartGameOutput startGamePVB(StartGameInput input) {
        for (Game game : games) {
            if (game.id == input.id && game.format == Format.PVB.getCode()) {
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
        throw new RuntimeException("blablabla")
    }

    StartGamePVPOutput startGamePVP(StartGamePVPInput input) {
        for (Game game : games) {
            if (game.id == input.id && game.format == Format.PVP.getCode()) {
                if (game.status.equals(Status.NOT_INITIALIZED.getCode())) {
                    initializeGamePVP(game)
                    boolean faceOrCrownResult = random.nextBoolean()
                    String winner = ""
                    if (input.player1faceOrCrown == input.player2faceOrCrown) {
                        throw new RuntimeException("Ambos escolheram o mesmo lado da moeda.")
                    } else if (input.player1faceOrCrown == faceOrCrownResult) {
                        winner = "${game.player1.name} Ganhou!"
                    } else if (input.player2faceOrCrown == faceOrCrownResult) {
                        winner = "${game.player2.name} Ganhou!"
                    }
                    return new StartGamePVPOutput(faceOrCrownResult: faceOrCrownResult, winner: winner)
                } else {
                    throw new RuntimeException("Partida com esse id ja foi iniciada.")
                }
            }
        }
        throw new RuntimeException("blablabla")
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
                            gameResult = "${game.player1.getName()}: jogou a carta ${playedCard.name} "
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
                            gameResult = "${game.player1.getName()}: passou a vez. Bot: passou a vez."
                        } else {
                            botAction = botService.handleBotTurn(botAction, game)
                            gameResult = "${game.player1.getName()}: passou a vez. Bot: jogou a carta ${botAction.botCardPlayed.name}"
                        }
                    }
                    if (game.bot.getPassTurn() && playerService.shouldPassTurn(input, game) && game.currentRound <= 3) {
                        startANewRound(game)
                        botService.resetPassTurn(game)
                    }
                    gameResult = "${gameResult}. Round atual = ${game.currentRound}"
                    if (game.player1.getLife() <= 0 || game.bot.getLife() <= 0 || game.currentRound > 3) {
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

    PlayGamePVPOutput playTheGamePVP(PlayPVPInput input) {
        for (Game game : games) {
            if (game.id == input.id && game.format == Format.PVP.getCode()) {
                if (game.status == Status.STARTED.getCode()) {
                    Card playerCard = null
                    String gameResult = ""
                    //StartGamePVPOutput startGamePVPOutput
                    if (input.idPlayer == game.player1.idPlayer) {
                        if (input.passTurn) {
                            gameResult = "${game.player1.getName()}: passou a vez."
                        } else {
                            input.index = game.player1.cards.findIndexOf { it.id == input.cardId }
                            if (input.index < 0) {
                                throw new RuntimeException("A carta que tentaste jogar é inválida")
                            }
                            playerCard = game.player1.cards[input.index]
                            game.player1.cards.remove(input.index)
                            game.player1.cardsPlayed[game.currentRound.toString()] = game.player1.cardsPlayed[game.currentRound.toString()] + playerCard
                            game.player1.attackPoints = game.player1.attackPoints + playerCard.attack
                            gameResult = "${game.player1.getName()}: jogou a carta ${playerCard.name} "
                        }
                    }
                    if (input.idPlayer == game.player2.idPlayer) {
                        if (input.passTurn) {
                            gameResult = "${game.player2.getName()}: passou a vez."
                        } else {
                            input.index = game.player2.cards.findIndexOf { it.id == input.cardId }
                            if (input.index < 0) {
                                throw new RuntimeException("A carta que tentaste jogar é inválida")
                            }
                            playerCard = game.player2.cards[input.index]
                            game.player2.cards.remove(input.index)
                            game.player2.cardsPlayed[game.currentRound.toString()] = game.player2.cardsPlayed[game.currentRound.toString()] + playerCard
                            game.player2.attackPoints = game.player2.attackPoints + playerCard.attack
                            gameResult = "${game.player2.getName()}: jogou a carta ${playerCard.name} "
                        }
                    }
                    if (game.player1.passTurn && game.player2.passTurn && game.currentRound <= 3) {
                        startANewRound(game)
                    }
                    gameResult = "${gameResult}. Round atual = ${game.currentRound}"
                    if (game.player1.getLife() <= 0 || game.player2.getLife() <= 0 || game.currentRound > 3) {
                        gameResult = getWinner(game)
                    }
                    return new PlayGamePVPOutput(playerCardPlayed: playerCard, gameResult: gameResult)
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

    private void initializeGamePVP(Game game) {
        game.status = Status.STARTED.getCode()
        game.currentRound = 1
        playerService.resetPlayerAttributesPVP(game)
    }

    private void startANewRound(Game game) {
        game.currentRound++
        if (game.player1.getAttackPoints() > game.bot.getAttackPoints()) {
            botService.loseLife(game)
        } else if (game.player1.getAttackPoints() < game.bot.getAttackPoints()) {
            playerService.loseLife(game)
        } else {
            botService.loseLife(game)
            playerService.loseLife(game)
        }
        playerService.resetAttackPoints(game)
        botService.resetAttackPoints(game)
    }

    private String getWinner(Game game) {
        if (game.player1.getLife() <= 0 && game.bot.getLife() <= 0) {
            game.status = Status.FINALIZED.getCode()
            return "Empatou o Jogo!"
        } else if (game.bot.getLife() <= 0) {
            game.status = Status.FINALIZED.getCode()
            return game.player1.getName() + " ganhou o Jogo!"
        } else {
            game.status = Status.FINALIZED.getCode()
            return "Bot ganhou o Jogo!"
        }
    }

}


