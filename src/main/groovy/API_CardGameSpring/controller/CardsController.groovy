package API_CardGameSpring.controller

import API_CardGameSpring.models.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/cards")
class CardsController {
    //Todo -> Arruma os efeitos colaterais
    //TODO UTILIZAR PROGRAMAÇÃO E ORIENTADA A OBJETOS
    private Map<String, Card> cards = [
            "1" : new Card(id: 1, name: "Cirilla", attack: 15, position: "MELEE", faction: "neutral"),
            "2" : new Card(id: 2, name: "Gerald", attack: 15, position: "MELEE", faction: "neutral"),
            "3" : new Card(id: 3, name: "Triss", attack: 7, position: "MELEE", faction: "neutral"),
            "4" : new Card(id: 4, name: "Vernon", attack: 10, position: "MELEE", faction: "Northern Realms"),
            "5" : new Card(id: 5, name: "Imlerith", attack: 10, position: "MELEE", faction: "Monsters"),
            "6" : new Card(id: 6, name: "Phillipa", attack: 10, position: "RANGED", faction: "Northern Realms"),
            "7" : new Card(id: 7, name: "Yennefer", attack: 7, position: "RANGED", faction: "neutral"),
            "8" : new Card(id: 8, name: "Milva", attack: 10, position: "RANGED", faction: "Scoia'tael"),
            "9" : new Card(id: 9, name: "Eithné", attack: 10, position: "RANGED", faction: "Scoia'tael"),
            "10": new Card(id: 10, name: "Iorveth", attack: 10, position: "RANGED", faction: "Scoia'tael"),
            "11": new Card(id: 11, name: "Catapult", attack: 8, position: "SIEGE", faction: "Northern Realms"),
            "12": new Card(id: 12, name: "Thaler", attack: 1, position: "SIEGE", faction: "Northern Realms"),
            "13": new Card(id: 13, name: "Fire Elemental", attack: 6, position: "SIEGE", faction: "Monsters"),
            "14": new Card(id: 14, name: "Morvran Voorhis", attack: 10, position: "SIEGE", faction: "NilfGaard"),
            "15": new Card(id: 15, name: "Gaunter O'Dimm", attack: 2, position: "SIEGE", faction: "Neutral")
    ]
    private Random random = new Random()
    private Bot bot = new Bot()
    private Player player = new Player()
    private GameLogic gameLogic = new GameLogic()


    @GetMapping
    ResponseEntity getCards() {
        return ResponseEntity.ok(cards.values().toList())
    }

    @PostMapping("add-cards")
    ResponseEntity createCard(@RequestBody Card newCard) {
        Card card = cards[newCard.id]
        if (card != null) {
            return ResponseEntity.status(409).build()
        }
        cards.put(newCard.id.toString(), newCard)
        return ResponseEntity.noContent().build()
    }


    @PostMapping("/start_game")
    ResponseEntity startGame(@RequestBody StartGameInput input) {
        initializeGame(input)
        BotAction botAction = new BotAction()
        boolean faceOrCrownResult = random.nextBoolean()
        if (input.faceOrCrown != faceOrCrownResult) {
            botAction = playBot()
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
        if (gameLogic.playerLife <= 0 || gameLogic.botLife <= 0) {
            return ResponseEntity.notFound().build()
        }
        List<BotAction> botActions = []
        player.passTurn = input.passTurn
        if (player.cards.isEmpty()) {
            player.passTurn = true
        }
        if (!player.passTurn) {
            if (cardIdInvalid(input)) {
                return ResponseEntity.badRequest().build()
            } else {
                return ResponseEntity.ok(handlePlayerTurn(input, botActions))
            }
        } else {
            return ResponseEntity.ok(handleBotTurn(botActions))
        }
    }

    @GetMapping("/status")
    ResponseEntity getStatus() {
        StatusGameOutput statusOutput = new StatusGameOutput(botCards: bot.cards, playerCards: player.cards,
                playerLife: gameLogic.playerLife.toString(), botLife: gameLogic.botLife.toString(), currentRound: gameLogic.currentRound,
                playerAttack: gameLogic.playerAttackPoints.toString(), botAttack: gameLogic.botAttackPoints.toString())
        return ResponseEntity.ok(statusOutput)
    }


    private void initializeGame(StartGameInput input) {
        gameLogic.playerLife = 2
        gameLogic.botLife = 2
        gameLogic.currentRound = 1
        gameLogic.playerAttackPoints = 0
        gameLogic.botAttackPoints = 0
        bot.cards = []
        player.cards = []
        player.name = input.player.name
        bot.cardsPlayed = ["1": [], "2": [], "3": []]
        giveRandomCards()
    }

    private void giveRandomCards() {
        for (int i = 0; i < 5; i++) {
            int id
            id = random.nextInt(cards.size()) + 1
            player.cards.add(cards.get(id.toString()))
            id = random.nextInt(cards.size()) + 1
            bot.cards.add(cards.get(id.toString()))
        }
    }

    private BotAction playBot() {
        int index = random.nextInt(bot.cards.size())
        Card botCardPlayed = bot.cards.get(index)
        bot.cards.remove(index)
        bot.cardsPlayed[gameLogic.currentRound.toString()] = bot.cardsPlayed[gameLogic.currentRound.toString()] + botCardPlayed
        gameLogic.botAttackPoints = gameLogic.botAttackPoints + botCardPlayed.attack
        return new BotAction(botCardPlayed: botCardPlayed)
    }

    private PlayGameOutput handleBotTurn(List<BotAction> botActions) {
        while (shouldBotPlay()) {
            if (gameLogic.botAttackPoints > gameLogic.botAttackPoints) {
                break
            } else {
                BotAction botAction = playBot()
                botActions.add(botAction)
            }
        }
        startANewRound()
        if (gameLogic.playerLife <= 0 && gameLogic.botLife <= 0) {
            String gameResult = "Empatou o Jogo!"
            PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
            return playOutput
        } else if (gameLogic.botLife <= 0) {
            String gameResult = player.name + " ganhou o Jogo!"
            PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
            return playOutput
        } else if (gameLogic.playerLife <= 0) {
            String gameResult = "Bot ganhou o Jogo!"
            PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
            return playOutput
        } else if (player.passTurn && botActions.size() > 0) {
            String gameResult = player.name + ": passou a vez. Bot: jogou a carta " +
                    botActions.botCardPlayed.name + " e depois passou a vez. novo round =" + gameLogic.currentRound
            PlayGameOutput playOutput = new PlayGameOutput(botActions: botActions, gameResult: gameResult)
            return playOutput
        } else {
            String gameResult = player.name + ": passou a vez. Bot: passou a vez. Novo round atual = " + gameLogic.currentRound
            PlayGameOutput playOutput = new PlayGameOutput(botActions: botActions, gameResult: gameResult)
            return playOutput
        }
    }


    private PlayGameOutput playBotTurn(Card playerCardPlayed, List<BotAction> botActions) {
        if (gameLogic.playerLife <= 0 && gameLogic.botLife <= 0) {
            String gameResult = "Empatou o Jogo!"
            PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
            return playOutput
        } else if (gameLogic.botLife <= 0) {
            String gameResult = player.name + " ganhou o Jogo!"
            PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
            return playOutput
        } else if (gameLogic.playerLife <= 0) {
            String gameResult = "Bot ganhou o Jogo!"
            PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
            return playOutput
        } else if (bot.passTurn) {
            String gameResult = player.name + ": jogou a carta " + playerCardPlayed.name + ". Bot: passou a vez." +
                    " Round atual = " + gameLogic.currentRound
            PlayGameOutput playOutput = new PlayGameOutput(playerCardPlayed: playerCardPlayed,
                    botActions: botActions, gameResult: gameResult)
            return playOutput
        } else {
            String gameResult = player.name + ": jogou a carta " + playerCardPlayed.name + ". Bot: jogou a carta " +
                    botActions.botCardPlayed.name + ". Round atual = " + gameLogic.currentRound
            PlayGameOutput playOutput = new PlayGameOutput(playerCardPlayed: playerCardPlayed,
                    botActions: botActions, gameResult: gameResult)
            return playOutput
        }
    }

    private void startANewRound() {
        if (gameLogic.playerAttackPoints > gameLogic.botAttackPoints) {
            gameLogic.botLife = gameLogic.botLife - 1
        } else if (gameLogic.botAttackPoints > gameLogic.playerAttackPoints) {
            gameLogic.playerLife = gameLogic.playerLife - 1
        } else {
            gameLogic.botLife= gameLogic.botLife - 1
            gameLogic.playerLife = gameLogic.playerLife - 1
        }
        gameLogic.currentRound++

        gameLogic.playerAttackPoints = 0
        gameLogic.botAttackPoints = 0
    }

    private boolean shouldBotPlay() {
        if (gameLogic.botLife == 1 && bot.cards.isEmpty()) {
            return false
        } else if (bot.cards.isEmpty()) {
            return false
        } else if (gameLogic.botLife == 1) {
            return true
        } else if (bot.passTurn) {
            return false
        }
        return random.nextBoolean()
    }


    private PlayGameOutput handlePlayerTurn(PlayInput input, List<BotAction> botActions) {
        Card playerCardPlayed = player.cards[input.index]
        player.cards.remove(input.index)
        player.cardsPlayed[gameLogic.currentRound.toString()] = player.cardsPlayed[gameLogic.currentRound.toString()] + playerCardPlayed
        gameLogic.playerAttackPoints = gameLogic.playerAttackPoints + playerCardPlayed.attack
        if (shouldBotPlay()) {
            bot.passTurn = false
            BotAction botAction = playBot()
            botActions.add(botAction)
            return playBotTurn(playerCardPlayed, botActions)
        } else {
            bot.passTurn = true
            startANewRound()
            return playBotTurn(playerCardPlayed, botActions)
        }
    }

    private boolean cardIdInvalid(PlayInput input) {
        input.index = player.cards.findIndexOf { it.id == input.cardId }
        if (input.index < 0) {
            return true
        }
        return false
    }

}



