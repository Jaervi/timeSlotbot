package s1.telegrambots
import s1.telegrambots.BasicBot
import scala.util.Using
import scala.io.Source

object YourBot extends App:
    object Bot extends BasicBot:
        
        /**
         * TODO: Luokaa bottinne tähän metodeineen ja reagoijineen.
         */

        this.run()
        // Tarkistetaan, että lähti käyntiin
        println("Started the bot")

    end Bot

    // Tämä rivi pyytää ja ajaa täten yllä olevan botin
    val bot = Bot 
end YourBot
