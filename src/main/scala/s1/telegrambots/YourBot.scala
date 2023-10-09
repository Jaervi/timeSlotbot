package s1.telegrambots
import s1.telegrambots.BasicBot
import s1.telegrambots.FilePreprocessor

import scala.collection.mutable
import scala.collection.mutable.Buffer
import scala.util.Using
import scala.io.Source
import java.io.*

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object YourBot extends App:
    object Bot extends BasicBot:

        var userCalendars = new mutable.HashMap()

        /**
         * TODO: Luokaa bottinne tähän metodeineen ja reagoijineen.
         */

        this.onUserMessage(FilePreprocessor.getFilepathsFromMessage)

        /* erkalle

        var isWaitingForMessage = false

        def replycom(msg: Message) =
            isWaitingForMessage = true
            val userid: Long = msg.from.get.id
            "etner time"

        this.onUserCommand("time", replycom)

        def mes(msg: Message) =
            if (isWaitingForMessage)
                println(msg.text)

        this.onUserMessage(mes)
        tähän asti */

        this.run()
        // Tarkistetaan, että lähti käyntiin
        println("Started the bot")

    end Bot

    // Tämä rivi pyytää ja ajaa täten yllä olevan botin
    val bot = Bot 
end YourBot
