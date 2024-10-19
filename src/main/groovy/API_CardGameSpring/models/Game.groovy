package API_CardGameSpring.models

class Game {
    Integer id
    String status = Status.NOT_INITIALIZED.getCode()
    Player player
    Character bot
    Integer currentRound

    Game(Integer id, Player player, Character bot, Integer currentRound) {
        this.id = id
        this.player = player
        this.bot = bot
        this.currentRound = currentRound
    }


}
