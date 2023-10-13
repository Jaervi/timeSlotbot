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

        this.onUserMessage(FilePreprocessor.parseFilepathsFromMessage)
        this.onUserCommand("time", replycom)
        onUserCommand("help", help)
        onUserCommand("When", when)

        def when(msg: Message) =
            writeMessage("Give first date", getChatId(msg))
            isWaitingForMessage = true
            var startingTime = msg.text
            writeMessage("give end date", getChatId(msg))
            isWaitingForMessage = true
            var endTime = msg.text
            s"Startingtime set as: ${startingTime.getOrElse("")} Endtime set: ${endTime.getOrElse("")}"


        def printfile(msg: Message): String =
            FilePreprocessor.getFile(msg.from.get.id) match
                case Some(file) =>
                    val cal = Calendar(FileHandler.eventsFromICSFile(file), 1)
                    cal.sortEventsByStartTime()
                    cal.printList()
                    "yes file :)"
                case None =>
                    println("no file :(")
                    "no file :("

        this.onUserCommand("file", printfile)

        var isWaitingForMessage = false

        def replycom(msg: Message) =
            isWaitingForMessage = true
            msg.from match
                case Some(value) => val userid: Long = msg.from.get.id
                case None =>
            "enter time"


/*
        def alku(s: Message)=
            var chatID = getChatId(s)
            var userId = s.from.get.id
            s"Mukava kun käytät bottia $chatID $userId"
        end alku
*/


        def mes(msg: Message) =
            if (isWaitingForMessage)
                println(msg.text)

        this.onUserMessage(mes)


        def help(s: Message) =
            "I'm a bot that an help you manage meeting times with your friends.\n " +
              "Start the bot with command /start" +
              "Here are the commands: \n " +
              "/time - determines the meeting duration \n" +
              "/help - takes you here \n"


        this.run()
        // Tarkistetaan, että lähti käyntiin
        println("Started the bot")

    end Bot

    // Tämä rivi pyytää ja ajaa täten yllä olevan botin
    val bot = Bot 
end YourBot
