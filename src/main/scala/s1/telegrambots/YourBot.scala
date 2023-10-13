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

object YourBot extends App:
    object Bot extends BasicBot:

        var usersInGroups = new HashMap[Long,Buffer[Long]]()
        var isWaitingForMessage = false

        this.onUserMessage(FilePreprocessor.parseFilepathsFromMessage)
        this.onUserCommandWithArguments("duration", replycom)
        onUserCommand("help", help)
        //onUserCommand("When", )
        onUserCommandWithArguments("When", when)


        def when(msg: Seq[String]) =
            var startingTime = msg.head
            var endTime = msg(1)
            s"Startingtime set as: ${startingTime} Endtime set: ${endTime}"

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
            end if
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
              "/duration - length of the meeting \n" +
              "/file -Just send your calendar as a .ics file to the bot privately then run this message to check that the file has been sent\n"



        this.run()
        // Tarkistetaan, että lähti käyntiin
        println("Started the bot")

    end Bot

    // Tämä rivi pyytää ja ajaa täten yllä olevan botin
    val bot = Bot 
end YourBot
