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
import java.util.concurrent.TimeUnit

object YourBot extends App:
    object Bot extends BasicBot:

        private var usersInGroups = new HashMap[Long,Buffer[Long]]()

        this.onUserMessage(FilePreprocessor.parseFilepathsFromMessage)
        onUserCommand("help", help)
        onUserCommand("when", when)
        onUserCommand("file", printfile)

        // Follow everything that happens in the server
        onUserExist(handleGroupMemberChanges)


        /**
         * Function that gets called on /when command
         * @param msg message
         * @return string that gets sent back as message, list of all possible timeslots
         */
        def when(msg: Message) =
            var userBufer = usersInGroups(getChatId(msg))
            var slotBuffer = Calendar(FileHandler.eventsFromICSFile(FilePreprocessor.getFile(userBufer(0)).get), java.util.Calendar.getInstance().getTimeInMillis/1000)
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
        end when

        /**
         * Logic behind /file command. Downloads and combines all sent files from this user.
         * @param msg message of /file command
         * @return Answering message about how many files were successfully read and also a meme
         */
        def printfile(msg: Message): String =
            var userid: Long = 0
            msg.from match
                case Some(user) => userid = user.id
                case None => return "Only humans can use this command"

            // Amount of files pending for processing
            var pendingAm = FilePreprocessor.isPending(userid)
            FilePreprocessor.getFile(userid) match
                case Some(file) =>
                    sendPhoto("nicefile.jpg", getChatId(msg))
                    if (FilePreprocessor.getLog < 100000) then
                        s"${FilePreprocessor.getLog} calendars successfully processed"
                    else
                        // Calculate the age of earlier file
                        var ageInHours: Double = (java.util.Calendar.getInstance().getTimeInMillis - FilePreprocessor.getLog) / 3600000.0
                        var ageInDays: Int = Math.floor(ageInHours / 24.0).toInt
                        ageInHours = Math.floor(ageInHours % 24)
                        if (ageInDays > 0) then
                            s"Found calendar sent $ageInDays days and ${ageInHours.toInt} hours ago"
                        else
                            s"Found calendar sent ${ageInHours.toInt} hours ago"
                case None =>
                    sendPhoto("nofiles.jpg", getChatId(msg))
                    if (pendingAm > 0) then "No sent calendar files found"
                    else "No files were found."
        end printfile


        /**
         * Add <userid> to <groupid> in usersIsGroup hashmap
         * @param userid userid
         * @param groupid groupid
         */
        def addUserToGroupBuffer(userid: Long, groupid: Long) =
            if (usersInGroups.contains(groupid)) then
                if (!usersInGroups(groupid).contains(userid)) then
                    usersInGroups(groupid) += userid
                end if
            else
                usersInGroups.addOne(groupid, Buffer[Long](userid))
            end if
        end addUserToGroupBuffer

        /**
         * Remove <userid> from <groupid> in usersIsGroup hashmap
         * @param userid userid
         * @param groupid groupid
         */
        def removeUserFromGroupBuffer(userid: Long, groupid: Long) =
            if (usersInGroups.contains(groupid)) then
                if (usersInGroups(groupid).contains(userid)) then
                    usersInGroups.remove(userid)
                end if
            end if
        end removeUserFromGroupBuffer

        /**
         * Handle adding or removing users from group member buffer
         * @param msg Message
          */
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

        /**
         * Function that gets called on /help command
         * @param s message
         * @return help message
         */
        def help(s: Message) =
            "I'm a bot that helps you manage meeting times with your friends.\n" +
              "Greet the bot with command /start \n" +
              "Here are the commands: \n" +
              "/when - Use this command AFTER everybody has sent their calendar to the bot. example use: /when 14,30 find possible 30min meeting times for the next 14 days\n" +
              "/help - takes you here \n" +
              "/file -Just send your calendar as a .ics file to the bot privately then run this message to check that the file has been sent. Note that the file can only be sent to the bot by direct messages\n"
        end help



        this.run()
        // Tarkistetaan, että lähti käyntiin
        println("Started the bot")

    end Bot

    // Tämä rivi pyytää ja ajaa täten yllä olevan botin
    val bot = Bot 
end YourBot
