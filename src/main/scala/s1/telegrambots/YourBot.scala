package s1.telegrambots
import s1.telegrambots.BasicBot
import s1.telegrambots.FilePreprocessor

import scala.collection.mutable
import scala.collection.mutable.{Buffer, HashMap}
import scala.util.Using
import scala.io.Source
import java.io.*
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import java.time.LocalDate

object YourBot extends App:
    object Bot extends BasicBot:

        var usersInGroups = new HashMap[Long,Buffer[Long]]()
        var isWaitingForMessage = false

        this.onUserMessage(FilePreprocessor.parseFilepathsFromMessage)
        //this.onUserCommandWithArguments("duration", replycom)
        onUserCommand("help", help)
        //onUserCommand("When", )
        onUserCommand("When", when)



        def when(msg: Message) =
            var userBufer = usersInGroups(getChatId(msg))
            var slotBuffer = Calendar(Buffer[CalendarEvent](), java.util.Calendar.getInstance().getTimeInMillis/1000)
            var viesti = getString(msg)
            var endTime = viesti.split(",")(0).toInt
            var duration = viesti.split(",")(1).toInt
            var muuttuja1 : File = null
            writeMessage(s"End time set as: ${endTime} duration set as: ${duration}",getChatId(msg))
            for id <- userBufer do
                muuttuja1 = null
                FilePreprocessor.getFile(id) match
                    case Some(file) => muuttuja1 = file
                    case None =>
                if muuttuja1 != null then
                    var event = Calendar(FileHandler.eventsFromICSFile(muuttuja1), java.util.Calendar.getInstance().getTimeInMillis/1000)
                    slotBuffer = slotBuffer.fuseTwoCalendars(event)
                    slotBuffer.removeCoveredEvents()
            end for
            slotBuffer.removeDayEvents()
            slotBuffer.limitEventsByDays(endTime)
            slotBuffer.filterForCurrentTime()
            slotBuffer.sortEventsByStartTime()
            slotBuffer.addNightLimits(23, 8, endTime + 2)
            slotBuffer = slotBuffer.fetchEmptySlots(slotBuffer, duration, endTime)
            var ajat : String = ""
            slotBuffer.eventList.foreach(ajat += _.toString + "\n")
            ajat

        def printfile(msg: Message): String =
            //FilePreprocessor.
            FilePreprocessor.getFile(msg.from.get.id) match
                case Some(file) =>
                    //val cal = Calendar(FileHandler.eventsFromICSFile(file), 1)
                    //cal.sortEventsByStartTime()
                    //cal.printList()
                    sendPhoto("nicefile.png", getChatId(msg))
                    "yes file :)"
                case None =>
                    sendPhoto("nofiles .jpg", getChatId(msg))
                    "No files were "

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
