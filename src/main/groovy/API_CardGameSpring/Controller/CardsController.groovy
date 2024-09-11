package API_CardGameSpring.Controller

import API_CardGameSpring.Models.Action
import API_CardGameSpring.Models.Bot
import API_CardGameSpring.Models.Card
import API_CardGameSpring.Models.Player
import API_CardGameSpring.Models.StartGameInput
import API_CardGameSpring.Models.StartGameOutput
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

    private Bot bot = new Bot()
    private Player player = new Player()

    @GetMapping
    ResponseEntity getCards() {
        return ResponseEntity.ok(cards.values().toList())
    }

    @PostMapping("/start_game")
    ResponseEntity startGame(@RequestBody StartGameInput input) {
        player = input.player
        Random random = new Random()
        Action botAction = new Action()
        boolean faceOrCrownResult = random.nextBoolean()
        for (int i = 0; i < 5; i++) {
            int id
            id = random.nextInt(cards.size()) + 1
            player.cards.add(cards.get(id.toString()))
            id = random.nextInt(cards.size()) + 1
            bot.cards.add(cards.get(id.toString()))
        }
        if (input.faceOrCrown != faceOrCrownResult) {
            botAction.cardPlayed = bot.cards.get(random.nextInt(bot.cards.size()))
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

    //@PostMapping("/play")
    //ResponseEntity play() {
    //
    //}


//    @GetMapping("/status")
//    ResponseEntity getStatus(){
//
//    }

}
