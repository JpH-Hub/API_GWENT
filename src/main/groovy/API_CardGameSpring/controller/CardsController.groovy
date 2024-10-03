package API_CardGameSpring.controller
import API_CardGameSpring.models.*
import API_CardGameSpring.services.BotService
import API_CardGameSpring.services.CardService
import API_CardGameSpring.services.LogicGameService
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
    //Todo -> Utilizar ENUM

    CardService cardService = new CardService()
    LogicGameService logicGameService = new LogicGameService()
    BotService botService = new BotService()
    PlayerService playerService = new PlayerService()

    @GetMapping
    ResponseEntity getCards() {
        return ResponseEntity.ok(cardService.cards.values().toList())
    }

    @PostMapping("add-cards")
    ResponseEntity createCard(@RequestBody Card newCard) {
        Card card = cardService.cards[newCard.id]
        if (card != null) {
            return ResponseEntity.status(409).build()
        }
        cardService.cards.put(newCard.id.toString(), newCard)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/start_game")
    ResponseEntity startGame(@RequestBody StartGameInput input) {
        logicGameService.initializeGame(input)
        BotAction botAction = new BotAction()
        boolean faceOrCrownResult = logicGameService.random.nextBoolean()
        if (input.faceOrCrown != faceOrCrownResult) {
            botAction = botService.playBot()
        }
        StartGameOutput output = new StartGameOutput(faceOrCrownResult: faceOrCrownResult, botAction: botAction)
        return ResponseEntity.ok(output)
    }

    @GetMapping("/player_cards")
    ResponseEntity getPlayerCards() {
        return ResponseEntity.ok(playerService.player.cards)
    }

    @GetMapping("/bot_cards")
    ResponseEntity getBotCards() {
        return ResponseEntity.ok(botService.bot.cards)
    }

    @PostMapping("/play")
    ResponseEntity play(@RequestBody PlayInput input) {
        if (playerService.player.life <= 0 || botService.bot.life <= 0) {
            return ResponseEntity.notFound().build()
        }
        List<BotAction> botActions = []
        playerService.player.passTurn = input.passTurn
        if (playerService.player.cards.isEmpty()) {
            playerService.player.passTurn = true
        }
        if (!playerService.player.passTurn) {
            if (playerService.cardIdInvalid(input)) {
                return ResponseEntity.badRequest().build()
            } else {
                return ResponseEntity.ok(playerService.handlePlayerTurn(input, botActions))
            }
        } else {
            return ResponseEntity.ok(botService.handleBotTurn(botActions))
        }
    }

    @GetMapping("/status")
    ResponseEntity getStatus() {
        StatusGameOutput statusOutput = new StatusGameOutput(botCards: botService.bot.cards, playerCards: playerService.player.cards,
                playerLife: playerService.player.life.toString(), botLife: botService.bot.life.toString(), currentRound: logicGameService.currentRound,
                playerAttack: playerService.player.attackPoints.toString(), botAttack: botService.bot.attackPoints.toString())
        return ResponseEntity.ok(statusOutput)
    }

}



