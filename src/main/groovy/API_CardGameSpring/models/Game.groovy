package API_CardGameSpring.models

class Game {
    Integer id
    String status = Status.NOT_INITIALIZED.getCode()
    String format
    Player player1
    Player player2
    Character bot
    Integer currentRound

    Game(Integer id, String format, Player player1, Player player2, Character bot, Integer currentRound) {
        this.id = id
        this.format = format
        this.player1 = player1
        this.player2 = player2
        this.bot = bot
        this.currentRound = currentRound
    }


}
