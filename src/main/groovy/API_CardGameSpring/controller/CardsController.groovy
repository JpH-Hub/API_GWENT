package API_CardGameSpring.controller

import API_CardGameSpring.models.*
import API_CardGameSpring.services.BotService
import API_CardGameSpring.services.CardService
import API_CardGameSpring.services.PlayerService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/cards")
class CardsController {
    private Random random = new Random()
    private Integer currentRound = 0
    private CardService cardService = new CardService(random)
    private BotService botService = new BotService(random, cardService)
    private PlayerService playerService = new PlayerService(cardService)
//      @TODO injetar dependencias usando @Configuration e @Bean
//    CardsController(Random random, CardService cardService, BotService botService, PlayerService playerService) {
//        this.random = random
//        this.cardService = cardService
//        this.botService = botService
//        this.playerService = playerService
//    }

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
            botAction = botService.throwCard(currentRound, playerService.getAttackPoints())
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
        List<BotAction> botActions = []
        Card playedCard = null
        String gameResult = ""
        if (playerService.getLife() <= 0 || botService.getLife() <= 0) {
            return ResponseEntity.notFound().build()
        }
        if (!playerService.shouldPassTurn(input)) {
            if (playerService.checkCardIdIsValid(input)) {
                playedCard = playerService.throwCard(input, currentRound)
                gameResult = "${playerService.getName()}: jogou a carta ${playedCard.name} "
                BotAction botAction = botService.throwCard(currentRound, playerService.getAttackPoints())
                gameResult = """
                        ${gameResult} ${botAction.passTurn ?
                        ". Bot: passou a vez."
                        : ". Bot: jogou a carta ${botAction.botCardPlayed.name}"}
                        .Round atual = ${currentRound}
                """
                botActions.add(botAction)
            } else {
                return ResponseEntity.badRequest().build()
            }
        } else {
            botActions = botService.handleBotTurn(playerService.getAttackPoints(), currentRound)
            gameResult = """
            ${botActions.passTurn} ? "${playerService.getName()}: passou a vez. Bot: passou a vez. Novo round = ${currentRound}" :
            "${playerService.getName()}: passou a vez. Bot: jogou a carta ${botActions.botCardPlayed.name}"
            e depois passou a vez. Novo round = ${currentRound}
            """
        }
        if (botActions.passTurn && input.passTurn && currentRound < 3) {
            startANewRound()
        } else if (currentRound > 3) {
            gameResult = getWinner()
        }
        return ResponseEntity.ok(new PlayGameOutput(botActions: botActions, playerCardPlayed: playedCard, gameResult: gameResult))
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
            botService.kill()
        } else if (playerService.getAttackPoints() < botService.getAttackPoints()) {
            playerService.kill()
        } else {
            botService.kill()
            playerService.kill()
        }
        playerService.resetAttackPoints()
        botService.resetAttackPoints()
    }

    private String getWinner() {
        if (playerService.getLife() <= 0 && botService.getLife() <= 0) {
            return "Empatou o Jogo!"
        } else if (botService.getLife() <= 0) {
            //@TODO fazer getName
            return playerService.getName() + " ganhou o Jogo!"
        } else {
            return "Bot ganhou o Jogo!"
        }
    }

}



