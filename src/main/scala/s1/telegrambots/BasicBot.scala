package s1.telegrambots

import scala.concurrent.Future
import scala.util.{Failure, Success, Try, Using}
import scala.io.Source
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.api.declarative.{Callbacks, Commands}
import com.bot4s.telegram.api.{RequestHandler, ChatActions}

import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.*
import com.bot4s.telegram.methods.{GetMe, SendMessage, *}
import com.bot4s.telegram.models.{InlineKeyboardButton, InlineKeyboardMarkup, InputFile, *}

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
  * A wrapper class that wraps more complicated or advanced functionality such as loading images and making
  * actual requests to Telegram API.
  * It is not necessary for the students taking this course to understand the contents of this file.
  */
class BasicBot extends TelegramBot with Polling with Commands[Future] with Callbacks[Future] with ChatActions[Future]:


    /**
     * Reads the token file 
     */
    def token = {

        val result = Using(Source.fromFile("bot_token.txt")){
            source => 
                val s = source.getLines().mkString.trim
                source.close()
                s
        }  // Not try here - if bot token does not exist this should fail
        
        result match
            case Success(res) => 
                res
            case Failure(error) =>
                Console.err.println("Error reading bot_token")
                error.printStackTrace()

                throw error
    }
    
    /**
    * Non-important function
    */
    override val client: RequestHandler[Future] = new ScalajHttpClient(token)

    private var chatId: ChatId = _
    private var message: Message = _
    private var data: String = _
    private var messageId: Int = _

    type Button = InlineKeyboardButton
    type Message = com.bot4s.telegram.models.Message

  /**
    * Extracts the text from a Message object
    *
    * @param msg A message containing text
    * @return The text in the message object. It it is a command, take only the parameter part as one long String.
    */

    def getString(msg: Message): String = 
        val str = msg.text.getOrElse("")
        if (str.startsWith("/"))
            str.dropWhile(_ != ' ').drop(1) // Drops everything until the first space, and then the space itself
        else
            str
    end getString  

    /**
      * Reacts to any messages on the channel sending back a new string. (Note that most channels don't allow this)
      *
      * @param action  A method that takes in a string and returns a new one
      * @return
      */

    def onUserMessageReplyString(action: String => String) = onMessage {
        implicit msg => 
            val msgString =  msg.text.mkString
            if msgString.length > 0 && msgString(0) != '/' then
                reply(action(msgString)).map(_=>())
            else
                Future(println("a command found"))
    }



    /**
      * Reacts to any messages on the channel sending back a new string. (Note that most channels don't allow this, but only give commands to bots)
      *
      * @param action  A method that takes in a message and returns a string as a reaction
      * @return
      */

    def onUserMessageReply(action: Message => String) = onMessage {
        implicit msg => 
            val msgString =  msg.text.mkString
            if msgString.length > 0 && msgString(0) != '/' then
                reply(action(msg)).map(_=>())
            else
                Future(println("a command found"))
    }


    /**
      * Reacts to any messages on the channel (Note that most channels don't allow this)
      *
      * @param action  A method that reacts in some way to a string on the channel
      * @return
      */
    def onUserMessageString(action: String => Unit) = onMessage {
        implicit msg => 
            val msgString =  msg.text.mkString
                if msgString.length > 0 && msgString(0) != '/' then
                    Future(action(msg.text.mkString))
                else
                    Future(println("a command found"))
    }

    /**
      * Reacts to any messages on the channel. (Note that most channels don't allow this, but only give commands to bots)
      *
      * @param action  A method that takes in a message. Note that the message object can be used to reply.
      * @return
      */

    def onUserMessage(action: Message => Unit)   = onMessage {
        implicit msg => 
            val msgString =  msg.text.mkString
            if msgString.nonEmpty && msgString(0) != '/' then
                Future(action(msg))
            else if (msg.document.isDefined) then
                Future(action(msg))
            else
                Future(println("a command found"))
    }

    /**
      * Reacts and responds to commands without arguments
      *
      * @param command The name of the command, e.g. "hello" for the command "/hello"
      * @param action  A method that returns a string to send as a reply
      * @return
      */
    def onUserCommand(command: String, action: Message => String) = onCommand(command) { 
        implicit msg => request(SendMessage(ChatId.fromChat(msg.chat.id), action(msg), parseMode = Some(ParseMode.HTML))).map(_ => ()) 
    }
    
    /**
      * Reacts and responds to a named command with arguments, i.e. extra words after command,
      * using the function provided as a parameter.
      *
      * @param command The name of the command, e.g. "hello" for the command "/hello"
      * @param action  A method that reacts to arguments and returns a string to send as a reply
      * @return
      */

    def onUserCommandWithArguments(command: String, action: Seq[String] => String) = onCommand(command) {
        implicit msg =>
            withArgs {
                args => request(SendMessage(msg.chat.id, action(args), parseMode = Some(ParseMode.HTML))).map(_->())
            }
    }

    /**
      * Reacts to new users joining the channel.
      *
      * @param action  A method that takes in a new user and returns a string to be broadcasted on the channel.
      * @return
      */

    def onJoinMessage(action: User => String) = 
        onMessage {
            implicit msg => Future {
                for {
                    members <- msg.newChatMembers
                    member  <- members
                } reply(action(member)).map(_ => ())
            }
        }
  

    /**
      * Sends the user a message with the given text. Can be used at any time.
      *
      * Note that chat ids can be extracted from any message using getChatId(message)
      *
      * @param text The text of the message to be sent to the user.
      * @return
      * @see getChatId(msg: Message)
      */
    def writeMessage(text: String, selectedChatId: ChatId) = {
        request(SendMessage(selectedChatId, text, parseMode = Some(ParseMode.HTML)))
    }

    
    /**
     * Sends a photo into the chat
     *
     * @param filename Filename of the photo
     * @param selectedChatId Chat id where to send the image
     */
    def sendPhoto(filename: String, selectedChatId: ChatId): Unit = {
        val photo = InputFile(java.nio.file.Paths.get(filename))
        request(SendPhoto(selectedChatId, photo))
    }

  /**
    * Sends the given buffered image into the chat
    * @param image Image that is to be sent
    * @param selectedChatId Chat id where to send the image
    */
    def sendBufferedImage(image: BufferedImage, selectedChatId: ChatId): Unit = {

        val imageOutputStream = new ByteArrayOutputStream()
        ImageIO.write(image, "png", imageOutputStream)
        val contents = com.bot4s.telegram.models.InputFile.Contents("image.png", imageOutputStream.toByteArray)

        request(SendPhoto(selectedChatId, contents))
    }

    /**
     * Extracts the first name (if any) of a user that sent a message.
     *
     * @param msg The message sent by the user
     * @return The username of the user
     */

    def getUserFirstName(msg: Message) = msg.from.map(_.firstName).mkString

    /**
     * Extracts a chat id from a message.
     *
     * A chat id can be used to later write text to the chat using the writeToChat
     * method.
     *
     * @param msg any message written into the chat
     * @return the chat id
     */

    def getChatId(msg: Message) = msg.chat.id


    def viestintiedot(msg: Message) =
      onUserMessageReply(msg => "moikku")

    /**
      * A built-in kill switch system. Disable it if needed by overriding killSwitch
      */
    def killSwitch() = 
        Console.err.println("Shutting down")
        System.exit(0)
        "Quitting"
    

    onUserCommand("kill", message => killSwitch())

end BasicBot
  
