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
            println("ollaan ttäälllä")
            var userBufer = usersInGroups(getChatId(msg))
            var slotBuffer = Calendar(FileHandler.eventsFromICSFile(FilePreprocessor.getFile(userBufer(0)).get), java.util.Calendar.getInstance().getTimeInMillis/1000)
            //var slotBuffer = Calendar(Buffer[CalendarEvent](), java.util.Calendar.getInstance().getTimeInMillis/1000)
            var viesti = getString(msg)
            println(viesti)
            var endTime = viesti.split(",")(0).toInt
            var duration = viesti.split(",")(1).toInt
            println(s"endtime: $endTime, duration: $duration")
            var muuttuja1 : File = null
            writeMessage(s"End time set as: ${endTime} duration set as: ${duration}",getChatId(msg))
            for id <- userBufer do
                muuttuja1 = null
                FilePreprocessor.getFile(id) match
                    case Some(file) => muuttuja1 = file
                    case None =>
                if muuttuja1 != null then
                    // TODO: MITÄ TAPAHTUU???? printit hajottaa koko homman.
                    var event = Calendar(FileHandler.eventsFromICSFile(muuttuja1), java.util.Calendar.getInstance().getTimeInMillis/1000)
                    //println("SLOTBUFFER KALENTERI ================")
                    //slotBuffer.printList()
                    //println("EVENT KALENTERI ================")
                    //event.printList()
                    slotBuffer.sortEventsByStartTime()
                    event.sortEventsByStartTime()
                    slotBuffer.filterForCurrentTime()
                    event.filterForCurrentTime()
                    slotBuffer = event.fuseTwoCalendars(slotBuffer)
                    //slotBuffer.removeCoveredEvents()
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
            var pendingAm = FilePreprocessor.isPending(msg.from.get.id)
            // TODO: käyteään tätä johonki
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

        def addUserToGroupBuffer(userid: Long, groupid: Long) =
            if (usersInGroups.contains(groupid)) then
                if (!usersInGroups(groupid).contains(userid)) then
                    usersInGroups(groupid) += userid
                end if
            else
                usersInGroups.addOne(groupid, Buffer[Long]())
        end addUserToGroupBuffer

        def removeUserFromGroupBuffer(userid: Long, groupid: Long) =
            if (usersInGroups.contains(groupid)) then
                if (usersInGroups(groupid).contains(userid)) then
                    usersInGroups.remove(userid)
                end if
            end if
        end removeUserFromGroupBuffer

        // Handle adding or removing users from group member buffer
        def handleGroupMemberChanges(msg: Message): Unit =
            val groupid = getChatId(msg)
            msg.newChatMembers match
                case Some(newMembers) =>
                    for i <- newMembers.indices do
                        addUserToGroupBuffer(newMembers(i).id, groupid)
                case None =>
            msg.from match
                case Some(user) => if (!user.isBot) then addUserToGroupBuffer(user.id, groupid)
                case None =>
            msg.leftChatMember match
                case Some(user) => removeUserFromGroupBuffer(user.id, groupid)
                case None =>
        end handleGroupMemberChanges

        // Follow everything that happens in the server
        onUserExist(handleGroupMemberChanges)



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
