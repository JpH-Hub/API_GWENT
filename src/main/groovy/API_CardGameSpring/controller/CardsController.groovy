package API_CardGameSpring.controller

import API_CardGameSpring.models.BotAction
import API_CardGameSpring.models.Bot
import API_CardGameSpring.models.Card
import API_CardGameSpring.models.PlayGameOutput
import API_CardGameSpring.models.PlayInput
import API_CardGameSpring.models.Player
import API_CardGameSpring.models.StartGameInput
import API_CardGameSpring.models.StartGameOutput
import API_CardGameSpring.models.StatusGameOutput
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
    private int paralized
    private boolean turn
    private Random random = new Random()
    private Bot bot = new Bot()
    private Player player = new Player()
    private int rounds = 0

    @GetMapping
    ResponseEntity getCards() {
        return ResponseEntity.ok(cards.values().toList())
    }

    @PostMapping("/start_game")
    ResponseEntity startGame(@RequestBody StartGameInput input) {
        rounds = 1
        player.attackPoints = 0
        bot.attackPoints = 0
        player.life = 2
        bot.life = 2
        bot.cards = []
        player = input.player
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
        bot.cardsPlayed[rounds.toString()] = bot.cardsPlayed[rounds.toString()] + botCardPlayed
        bot.attackPoints =+ botCardPlayed.attack
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

    private ResponseEntity handlePlayerTurn(PlayInput input, List<BotAction> botActions) {
        int index = player.cards.findIndexOf { it.id == input.cardId }
        if (index < 0) {
            return ResponseEntity.badRequest().build()
        }
        Card playerCardPlayed = player.cards[index]
        player.cards.remove(index)
        player.cardsPlayed[rounds.toString()] = player.cardsPlayed[rounds.toString()] + playerCardPlayed
        player.attackPoints =+ playerCardPlayed.attack
        if (shouldBotPlay()) {
            paralized = 1
            playerCardPlayed = player.cards[index]
            //player.cards.remove(index)
            //player.cardsPlayed[rounds.toString()] = player.cardsPlayed[rounds.toString()] + playerCardPlayed
            //player.attackPoints += playerCardPlayed.attack
            if (turn) {
                paralized = 0
                if (player.attackPoints > bot.attackPoints) {
                    finishround()
                    bot.life =- bot.life
                } else if (bot.attackPoints > player.attackPoints) {
                    finishround()
                    player.life =- player.life
                } else {
                    finishround()
                    bot.life =- bot.life
                    player.life =- player.life
                }
            }
            return playBotTurn(playerCardPlayed, botActions)
        } else {
            BotAction botAction = playBot()
            botActions.add(botAction)
            return playBotTurn(playerCardPlayed, botActions)
        }
    }

    private boolean shouldBotPlay() {
        if (paralized == 1) {
            return true
        }
        return bot.cards.isEmpty() || random.nextBoolean()
    }

    private void finishround() {
        player.attackPoints = 0
        bot.attackPoints = 0
        rounds++
    }

    private ResponseEntity playBotTurn(Card playerCardPlayed, List<BotAction> botActions) {
        PlayGameOutput playOutput = new PlayGameOutput(playerCardPlayed: playerCardPlayed, botActions: botActions)
        return ResponseEntity.ok(playOutput)
    }

    private ResponseEntity handleBotTurn(List<BotAction> botActions) {
        while (!shouldBotPlay()) {
            shouldBotPlay()
            BotAction botAction = playBot()
            botActions.add(botAction)
        }
        if (player.attackPoints > bot.attackPoints) {
            finishround()
            bot.life =- bot.life
        } else if (bot.attackPoints > player.attackPoints) {
            finishround()
            player.life =- player.life
        } else {
            finishround()
            bot.life =- bot.life
            player.life =- player.life
        }
        PlayGameOutput playOutput = new PlayGameOutput(botActions: botActions)
        return ResponseEntity.ok(playOutput)
    }

    @PostMapping("/play")
    ResponseEntity play(@RequestBody PlayInput input) {
        if (bot.life <= 0) {
            String winner = "Você ganhou"
            return ResponseEntity.ok(winner)
        } else if (player.life == 0) {
            String winner = "O BOT ENFIOU O DEDO NO SEU CU ganhou!"
            return ResponseEntity.ok(winner)
        } else if (player.life <= 0 && bot.life <= 0) {
            String winner = "Empate!"
            return ResponseEntity.ok(winner)
        }
        List<BotAction> botActions = []
        turn = input.passTurn
        if (player.cards.isEmpty()) {
            turn = true
        }
        if (!turn) {
            return handlePlayerTurn(input, botActions)
        } else {
            return handleBotTurn(botActions)
        }
    }

// quando o turno encerrar, a rota /play deveria retornar o resultado dele turno
// TALVEZ deva existir uma rota start_round
// o contador de round tem que ser incrementado ao fim de cada round
// quando a partida acabar, a rota /play deve retornar o resultado da partida

    //@GetMapping("/status")
    //ResponseEntity getStatus() {
        //StatusGameOutput statusOutPut = new StatusGameOutput(botCards: botCards, playerCards: playerCards,
        //        playerLife: , botLife:, rounds: rounds)
        //@TODO deve retornar o status atual da partida, cartas disponiveis do player, round atual,
        // placar e o que mais for improtante
        //return org.springframework.http.ResponseEntity.ok(bot.cards)
        //return org.springframework.http.ResponseEntity.ok(player.cards)
        //return org.springframework.http.ResponseEntity.ok(player.life)
        //return ResponseEntity.ok(bot.life)
        //return ResponseEntity.ok(rounds)
        //return ResponseEntity.ok(statusOutput)
    //}
}



