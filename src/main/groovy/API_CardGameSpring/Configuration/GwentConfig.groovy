package API_CardGameSpring.Configuration

import API_CardGameSpring.controller.CardsController
import API_CardGameSpring.services.BotService
import API_CardGameSpring.services.CardService
import API_CardGameSpring.services.PlayerService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class GwentConfig {


    @Bean
    CardsController cardsController(){
        return new CardsController(random(), cardService(), botService(), playerService())
    }
    @Bean
    BotService botService() {
        return new BotService(random(), cardService())
    }


    @Bean
    PlayerService playerService() {
        return new PlayerService(cardService())
    }

    @Bean
    CardService cardService() {
        return new CardService(random())
    }

    @Bean
    Random random() {
        return new Random()
    }
}
