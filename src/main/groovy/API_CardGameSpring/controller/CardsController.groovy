package API_CardGameSpring.controller

import API_CardGameSpring.models.*
import API_CardGameSpring.models.Input.GameInput
import API_CardGameSpring.models.Input.PlayInput
import API_CardGameSpring.models.Input.StartGameInput
import API_CardGameSpring.models.Output.PlayGameOutput
import API_CardGameSpring.models.Output.ResponseOutput
import API_CardGameSpring.models.Output.StartGameOutput
import API_CardGameSpring.models.Output.StatusGameOutput
import API_CardGameSpring.services.BotService
import API_CardGameSpring.services.CardService
import API_CardGameSpring.services.GameService
import API_CardGameSpring.services.PlayerService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

//TODO REALIZAR ISSO PRIMEIRO: Criar uma service, chamada GameService e com isso reduzir a logica da controller.(CONCLUIDO)


//TODO fazer com que o gwent possa suportar mais de uma partida. (Concluida :))


//TODO fazer alguma forma com que podemos jogar player contra player.


//TODO ter uma rota que retorne uma imagem.


//se quisermos podemos usar banco de dados.
@RestController
@RequestMapping("/cards")
class CardsController {

    private BotService botService
    private PlayerService playerService
    private CardService cardService
    private GameService gameService

    CardsController(CardService cardService, BotService botService, PlayerService playerService, GameService gameService) {
        this.botService = botService
        this.playerService = playerService
        this.cardService = cardService
        this.gameService = gameService
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

    @PostMapping("/game")
    ResponseEntity registerGame(@RequestBody GameInput input ) {
        try{
            ResponseOutput responseOutput = gameService.registerGame(input)
            return ResponseEntity.status(HttpStatus.OK).body(responseOutput)
        }
        catch (RuntimeException e){
            ResponseOutput responseOutput = new ResponseOutput(message: e.getMessage())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseOutput)
        }
    }
    @GetMapping("/games")
    ResponseEntity getGames(){
        return ResponseEntity.ok(gameService.games)
    }
    @PostMapping("/start_game")
    ResponseEntity startGame(@RequestBody StartGameInput input) {
        try {
            StartGameOutput output = gameService.startGame(input)
            return ResponseEntity.ok(output)
        } catch (RuntimeException e) {
            ResponseOutput responseOutput = new ResponseOutput(message: e.getMessage())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseOutput)
        }

    }

    @GetMapping("/player_cards/{id}")
    ResponseEntity getPlayerCards(@PathVariable("id") Integer idMatch) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(playerService.getCards(idMatch, gameService.games))
        } catch (RuntimeException e) {
            ResponseOutput responseOutput = new ResponseOutput(message: e.getMessage())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseOutput)
        }

    }

    @GetMapping("/bot_cards")
    ResponseEntity getBotCards() {
        //Todo - para o joão fazer, seguir a mesma logica que o getPlayerCards!
        return ResponseEntity.ok(botService.getCards())
    }

    @PostMapping("/play")
    ResponseEntity play(@RequestBody PlayInput input) {
        try {
            PlayGameOutput output = gameService.playTheGame(input)
            return ResponseEntity.status(HttpStatus.OK).body(output)
        }
        catch (RuntimeException e) {
            ResponseOutput output = new ResponseOutput(message: e.getMessage())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(output)
        }
    }

    @GetMapping("/status")
    ResponseEntity getStatus() {
        StatusGameOutput statusOutput = new StatusGameOutput(botCards: botService.getCards(), playerCards: playerService.getCards(),
                playerLife: playerService.getLife().toString(), botLife: botService.getLife().toString(), currentRound: gameService.currentRound,
                playerAttack: playerService.getAttackPoints().toString(), botAttack: botService.getAttackPoints().toString())
        return ResponseEntity.ok(statusOutput)
    }


}



