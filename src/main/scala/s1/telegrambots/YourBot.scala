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
        var isWaitingForMessage = false

        this.onUserMessage(FilePreprocessor.parseFilepathsFromMessage)
        //this.onUserCommandWithArguments("duration", replycom)
        onUserCommand("help", help)
        //onUserCommand("When", )
        onUserCommandWithArguments("When", when)


        def when(msg: Seq[String]) =
            var endTime = msg.head
            var duration = msg(1)
            s"End time set as: ${endTime} duration set as: ${duration}"

        /*def when(msg: Message) =
            isWaitingForMessage = true
            writeMessage("Give first date", getChatId(msg))
            isWaitingForMessage = true
            var startingTime =
            writeMessage("give end date", getChatId(msg))
            isWaitingForMessage = false
            //isWaitingForMessage = true
            var endTime = getString(msg)
            s"Startingtime set as: ${startingTime} Endtime set: ${endTime}"*/


        def printfile(msg: Message): String =
            FilePreprocessor.getFile(msg.from.get.id) match
                case Some(file) =>
                    val cal = Calendar(FileHandler.eventsFromICSFile(file), 1)
                    cal.sortEventsByStartTime()
                    cal.printList()
                    "yes file :)"
                case None =>
                    println("no file :(")
                    sendPhoto("nofiles .jpg", getChatId(msg))
                    "no file :("

        this.onUserCommand("file", printfile)



        def replycom(msg: Seq[String]) =
            var duration = msg.head
            s"meeting duration set as $duration"




        def mes(msg: Message) =
            if (isWaitingForMessage)
                println(msg.text)

        this.onUserMessage(mes)


        def help(s: Message) =
            "I'm a bot that helps you manage meeting times with your friends.\n " +
              "Start the bot with command /start \n" +
              "Here are the commands: \n " +
              "/when - lets user determine the possible days when the meeting will be held. \n" +
              "/help - takes you here \n" +
              "/file -Just send your calendar as a .ics file to the bot privately then run this message to check that the file has been sent\n"



        this.run()
        // Tarkistetaan, että lähti käyntiin
        println("Started the bot")

    end Bot

    // Tämä rivi pyytää ja ajaa täten yllä olevan botin
    val bot = Bot 
end YourBot
