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
    //Todo -> Arruma os efeitos colaterais
    //TODO -> Utilizar PROGRAMAÇÃO ORIENTADA A OBJETOS
    //TODO -> Utilizar MVC
    //Todo -> Utilizar IoC e DI
    private Random random = new Random()
    private Integer currentRound = 0
    private boolean faceOrCrownWin
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
        Card card = cardService.cards[newCard.id]
        if (card != null) {
            return ResponseEntity.status(409).build()
        }
        cardService.addCards(card)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/start_game")
    ResponseEntity startGame(@RequestBody StartGameInput input) {
        initializeGame(input)
        BotAction botAction = new BotAction()
        boolean faceOrCrownResult = random.nextBoolean()
        faceOrCrownWin = input.faceOrCrown == faceOrCrownResult
        if (!faceOrCrownWin) {
            botAction = botService.throwCard(currentRound, playerService.getAttackPoints())
        }
        StartGameOutput output = new StartGameOutput(faceOrCrownResult: faceOrCrownResult, botAction: botAction)
        return ResponseEntity.ok(output)
    }

    @GetMapping("/player_cards")
    ResponseEntity getPlayerCards() {
        return ResponseEntity.ok(player.cards)
    }

    @GetMapping("/bot_cards")
    ResponseEntity getBotCards() {
        return ResponseEntity.ok(bot.cards)
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
                         . Round atual = ${currentRound}
                """
                botActions.add(botAction)
            } else {
                return ResponseEntity.badRequest().build()
            }
        } else {
            botActions = botService.handleBotTurn(playerService.getAttackPoints(), currentRound)
            gameResult = """${playerService.getName()}""" //@TODO pensar em um jeito bom de mostrar as varias cartas
        }
        if (currentRound < 3) {
            startANewRound()
        } else {
            gameResult = getWinner()
        }
        return ResponseEntity.ok(new PlayGameOutput(botActions: botActions, playerCardPlayed: playedCard, gameResult: gameResult))
    }

    @GetMapping("/status")
    ResponseEntity getStatus() {
        StatusGameOutput statusOutput = new StatusGameOutput(botCards: bot.cards, playerCards: player.cards,
                playerLife: player.life.toString(), botLife: bot.life.toString(), currentRound: currentRound,
                playerAttack: player.attackPoints.toString(), botAttack: bot.attackPoints.toString())
        return ResponseEntity.ok(statusOutput)
    }

    private void initializeGame(StartGameInput input) {
        currentRound = 1

        //@TODO implementar métodos reset player  e reset bot dentro dos seus respectivos services,
        input.player.life = 2
        bot.life = 2
        player.attackPoints = 0
        bot.attackPoints = 0
        bot.cards = cardService.giveRandomCards()
        player.cards = cardService.giveRandomCards()
        player.name = input.player.name
        bot.cardsPlayed = ["1": [], "2": [], "3": []]
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
        } else if (bot.life <= 0) {
            //@TODO fazer getName
            return playerService.getName() + " ganhou o Jogo!"
        } else {
            return "Bot ganhou o Jogo!"
        }
    }

}



