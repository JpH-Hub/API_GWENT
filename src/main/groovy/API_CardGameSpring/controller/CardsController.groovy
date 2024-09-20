package API_CardGameSpring.controller

import API_CardGameSpring.models.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/cards")
class CardsController {
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
            "10": new Card(id: 10, name: "Iorvet", attack: 10, position: "RANGED", faction: "Scoia'tael"),
            "11": new Card(id: 11, name: "Catapult", attack: 8, position: "SIEGE", faction: "Northern Realms"),
            "12": new Card(id: 12, name: "Thaler", attack: 1, position: "SIEGE", faction: "Northern Realms"),
            "13": new Card(id: 13, name: "Fire Elemental", attack: 6, position: "SIEGE", faction: "Mosters"),
            "14": new Card(id: 14, name: "Morvran Voorhis", attack: 10, position: "SIEGE", faction: "NilfGaard"),
            "15": new Card(id: 15, name: "Gaunter O'Dimm", attack: 2, position: "SIEGE", faction: "Neutral")
    ]
    private int botTurnPassed
    private boolean turn
    private Random random = new Random()
    private Bot bot = new Bot()
    private Player player = new Player()
    private int currentRound = 0

    @GetMapping
    ResponseEntity getCards() {
        return ResponseEntity.ok(cards.values().toList())
    }

    @PostMapping("/start_game")
    ResponseEntity startGame(@RequestBody StartGameInput input) {
        currentRound = 1
        player.attackPoints = 0
        bot.attackPoints = 0
        player.life = 2
        bot.life = 2
        bot.cards = []
        player.cards = []
        player.name = input.player.name
        bot.cardsPlayed = ["1": [], "2": [], "3": []]
        BotAction botAction = new BotAction()
        boolean faceOrCrownResult = random.nextBoolean()
        for (int i = 0; i < 5; i++) {
            int id
            id = random.nextInt(cards.size()) + 1
            player.cards.add(cards.get(id.toString()))
            id = random.nextInt(cards.size()) + 1
            bot.cards.add(cards.get(id.toString()))
        }
        if (input.faceOrCrown != faceOrCrownResult) {
            botAction = playBot()
        }
        StartGameOutput output = new StartGameOutput(faceOrCrownResult: faceOrCrownResult, botAction: botAction)
        return ResponseEntity.ok(output)
    }

    private BotAction playBot() {
        int index = random.nextInt(bot.cards.size())
        Card botCardPlayed = bot.cards.get(index)
        bot.cards.remove(index)
        bot.cardsPlayed[currentRound.toString()] = bot.cardsPlayed[currentRound.toString()] + botCardPlayed
        bot.attackPoints = bot.attackPoints + botCardPlayed.attack
        return new BotAction(botCardPlayed: botCardPlayed)
    }


    @GetMapping("/player_cards")
    ResponseEntity getPlayerCards() {
        return ResponseEntity.ok(player.cards)
    }

    @GetMapping("/bot_cards")
    ResponseEntity getBotCards() {
        return ResponseEntity.ok(bot.cards)
    }

    private boolean invalidCard(PlayInput input) {
        input.index = player.cards.findIndexOf { it.id == input.cardId }
        if (input.index < 0) {
            return true
        }
        return false
    }

    private PlayGameOutput handlePlayerTurn(PlayInput input, List<BotAction> botActions) {
        Card playerCardPlayed = player.cards[input.index]
        player.cards.remove(input.index)
        player.cardsPlayed[currentRound.toString()] = player.cardsPlayed[currentRound.toString()] + playerCardPlayed
        player.attackPoints = player.attackPoints + playerCardPlayed.attack
        if (shouldBotPlay()) {
            botTurnPassed = 1
            if (turn) {
                if (player.attackPoints > bot.attackPoints) {
                    finishround()
                    bot.life = bot.life - 1
                } else if (bot.attackPoints > player.attackPoints) {
                    finishround()
                    player.life = player.life - 1
                } else {
                    finishround()
                    bot.life = bot.life - 1
                    player.life = player.life - 1
                }
            }
            if (player.life <= 0 && bot.life <= 0) {
                String gameResult = "Empatou o Jogo!"
                PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
                return playOutput
            } else if (bot.life <= 0) {
               String  gameResult = player.name + " ganhou o Jogo!"
                PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
                return playOutput
            } else if (player.life <= 0) {
                String gameResult = "Bot ganhou o Jogo!"
                PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
                return playOutput
            }
            botTurnPassed = 0
            return playBotTurn(playerCardPlayed, botActions)
        } else {
            BotAction botAction = playBot()
            botActions.add(botAction)
            if (player.life <= 0 && bot.life <= 0) {
               String gameResult = "Empatou o Jogo!"
                PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
                return playOutput
            } else if (bot.life <= 0) {
               String gameResult = player.name + " ganhou o Jogo!"
                PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
                return playOutput
            } else if (player.life <= 0) {
               String gameResult = "Bot ganhou o Jogo!"
                PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
                return playOutput
            }
            return playBotTurn(playerCardPlayed, botActions)
        }
    }

    private boolean shouldBotPlay() {
        if (bot.life == 1 && bot.cards.isEmpty()) {
            return true
        } else if (bot.life == 1) {
            return false
        }
        return botTurnPassed == 1 || bot.cards.isEmpty() || random.nextBoolean()
    }

    private void finishround() {
        player.attackPoints = 0
        bot.attackPoints = 0
        currentRound++
    }

    private PlayGameOutput playBotTurn(Card playerCardPlayed, List<BotAction> botActions) {
        PlayGameOutput playOutput = new PlayGameOutput(playerCardPlayed: playerCardPlayed, botActions: botActions)
        return playOutput
    }

    private PlayGameOutput handleBotTurn(List<BotAction> botActions) {
        while (!shouldBotPlay()) {
            if (bot.attackPoints > player.attackPoints) {
                break
            } else {
                shouldBotPlay()
                BotAction botAction = playBot()
                botActions.add(botAction)
            }
        }
        if (player.attackPoints > bot.attackPoints) {
            finishround()
            bot.life = bot.life - 1
        } else if (bot.attackPoints > player.attackPoints) {
            finishround()
            player.life = player.life - 1
        } else {
            finishround()
            bot.life = bot.life - 1
            player.life = player.life - 1
        }
        if (player.life <= 0 && bot.life <= 0) {
           String gameResult = "Empatou o Jogo!"
            PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
            return playOutput
        } else if (bot.life <= 0) {
            String gameResult = player.name + " ganhou o Jogo!"
            PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
            return playOutput
        } else if (player.life <= 0) {
            String gameResult = "Bot ganhou o Jogo!"
            PlayGameOutput playOutput = new PlayGameOutput(gameResult: gameResult)
            return playOutput
        }
        PlayGameOutput playOutput = new PlayGameOutput(botActions: botActions)
        return playOutput
    }

    @PostMapping("/play")
    ResponseEntity play(@RequestBody PlayInput input) {

        List<BotAction> botActions = []
        turn = input.passTurn
        if (player.cards.isEmpty()) {
            turn = true
        }
        if (!turn) {
            invalidCard(input)
            if (invalidCard(input)) {
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
                playerLife: player.life.toString(), botLife: bot.life.toString(), currentRound: currentRound,
                playerAttack: player.attackPoints.toString(), botAttack: bot.attackPoints.toString())
        return ResponseEntity.ok(statusOutput)
    }
}



