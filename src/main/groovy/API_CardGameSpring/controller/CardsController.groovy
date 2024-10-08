package API_CardGameSpring.controller

import API_CardGameSpring.models.*
import API_CardGameSpring.models.Input.PlayInput
import API_CardGameSpring.models.Input.StartGameInput
import API_CardGameSpring.models.Output.PlayGameOutput
import API_CardGameSpring.models.Output.StartGameOutput
import API_CardGameSpring.models.Output.StatusGameOutput
import API_CardGameSpring.services.BotService
import API_CardGameSpring.services.CardService
import API_CardGameSpring.services.PlayerService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
//TODO REALIZAR ISSO PRIMEIRO: Criar uma service, chamada GameService e com isso reduzir a logica da controller.


//TODO fazer com que o gwent possa suportar mais de uma partida.


//TODO fazer alguma forma com que podemos jogar player contra player.


//TODO ter uma rota que retorne uma imagem.


//se quisermos podemos usar banco de dados.
@RestController
@RequestMapping("/cards")
class CardsController {
    private Random random
    private Integer currentRound = 0
    private CardService cardService
    private BotService botService
    private PlayerService playerService

    CardsController(Random random, CardService cardService, BotService botService, PlayerService playerService) {
        this.random = random
        this.cardService = cardService
        this.botService = botService
        this.playerService = playerService
    }

    @GetMapping
    ResponseEntity getCards() {
        return ResponseEntity.ok(cardService.cards.values().toList())
    }

    @PostMapping("card")
    ResponseEntity createCard(@RequestBody Card newCard) {
        Card card = cardService.cards[newCard.id.toString()]
        if (card != null) {
            return ResponseEntity.status(409).build()
        }
        cardService.addCards(newCard)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/start_game")
    ResponseEntity startGame(@RequestBody StartGameInput input) {
        initializeGame(input)
        BotAction botAction = new BotAction()
        boolean faceOrCrownResult = random.nextBoolean()
        if (input.faceOrCrown != faceOrCrownResult) {
            botAction = botService.throwCard(currentRound)
        }
        StartGameOutput output = new StartGameOutput(faceOrCrownResult: faceOrCrownResult, botAction: botAction)
        return ResponseEntity.ok(output)
    }

    @GetMapping("/player_cards")
    ResponseEntity getPlayerCards() {
        return ResponseEntity.ok(playerService.getCards())
    }

    @GetMapping("/bot_cards")
    ResponseEntity getBotCards() {
        return ResponseEntity.ok(botService.getCards())
    }

    @PostMapping("/play")
    ResponseEntity play(@RequestBody PlayInput input) {
        BotAction botAction = new BotAction()
        Card playedCard = null
        String gameResult = ""
        if (playerService.getLife() <= 0 || botService.getLife() <= 0) {
            return ResponseEntity.notFound().build()
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
                return ResponseEntity.badRequest().build()
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
        return ResponseEntity.ok(new PlayGameOutput(botAction: botAction, playerCardPlayed: playedCard, gameResult: gameResult))
    }

    @GetMapping("/status")
    ResponseEntity getStatus() {
        StatusGameOutput statusOutput = new StatusGameOutput(botCards: botService.getCards(), playerCards: playerService.getCards(),
                playerLife: playerService.getLife().toString(), botLife: botService.getLife().toString(), currentRound: currentRound,
                playerAttack: playerService.getAttackPoints().toString(), botAttack: botService.getAttackPoints().toString())
        return ResponseEntity.ok(statusOutput)
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



