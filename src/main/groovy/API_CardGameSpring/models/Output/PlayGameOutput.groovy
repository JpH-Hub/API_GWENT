package API_CardGameSpring.models.Output

import API_CardGameSpring.models.BotAction
import API_CardGameSpring.models.Card

class PlayGameOutput {
    Card playerCardPlayed
    BotAction botAction
    String gameResult
}
