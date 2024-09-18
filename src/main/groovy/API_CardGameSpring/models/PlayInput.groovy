package API_CardGameSpring.models

import com.fasterxml.jackson.annotation.JsonProperty

class PlayInput {
    Integer cardId
    boolean passTurn
    @JsonProperty("disciple_name")
    String winner

}
