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

    private Player player = new Player()
    private Bot bot = new Bot()
    private Integer currentRound = 0
    private Random random = new Random()
    private CardService cardService = new CardService(random)
    private BotService botService = new BotService(random)
    private PlayerService playerService = new PlayerService()

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
        initializeGame(input)
        BotAction botAction = new BotAction()
        boolean faceOrCrownResult = random.nextBoolean()
        if (input.faceOrCrown != faceOrCrownResult) {
            botAction = botService.playBot()
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
        if (player.life <= 0 || bot.life <= 0) {
            return ResponseEntity.notFound().build()
        }
        List<BotAction> botActions = []
        player.passTurn = input.passTurn
        if (player.cards.isEmpty()) {
            player.passTurn = true
        }
        if (!player.passTurn) {
            if (playerService.checkCardIdIsValid(input)) {
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
        StatusGameOutput statusOutput = new StatusGameOutput(botCards: bot.cards, playerCards: player.cards,
                playerLife: player.life.toString(), botLife: bot.life.toString(), currentRound: currentRound,
                playerAttack: player.attackPoints.toString(), botAttack: bot.attackPoints.toString())
        return ResponseEntity.ok(statusOutput)
    }

    private void initializeGame(StartGameInput input) {
        input.player.life = 2
        bot.life = 2
        currentRound = 1
        player.attackPoints = 0
        bot.attackPoints = 0
        bot.cards = cardService.giveRandomCards()
        player.cards = cardService.giveRandomCards()
        player.name = input.player.name
        bot.cardsPlayed = ["1": [], "2": [], "3": []]
    }

    private void startANewRound() {
        if (player.attackPoints > bot.attackPoints) {
            bot.life = bot.life - 1
        } else if (bot.attackPoints > player.attackPoints) {
            player.life = player.life - 1
        } else {
            bot.life = bot.life - 1
            player.life = player.life - 1
        }
        currentRound++
        player.attackPoints = 0
        bot.attackPoints = 0
    }

    private PlayGameOutput setWinner() {
        if (player.life <= 0 && bot.life <= 0 ) {
            String gameResult = "Empatou o Jogo!"
            PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
            return playOutput
        } else if (bot.life <= 0 ) {
            String gameResult = player.name + " ganhou o Jogo!"
            PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
            return playOutput
        } else {
            String gameResult = "Bot ganhou o Jogo!"
            PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
            return playOutput
        }
    }

}



