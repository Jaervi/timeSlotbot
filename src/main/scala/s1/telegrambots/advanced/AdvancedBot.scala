package s1.telegrambots.advanced
import s1.telegrambots.BasicBot

import scala.concurrent.Future

import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.api.declarative.{Commands, Callbacks}
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.clients.ScalajHttpClient

import com.bot4s.telegram.api.ChatActions
import com.bot4s.telegram._
import com.bot4s.telegram.methods.{SendMessage, _}
import com.bot4s.telegram.models.{InlineKeyboardButton, InlineKeyboardMarkup, InputFile, _}

/**
  * A wrapper class that wraps more complicated or advanced functionality such as loading images and making
  * actual requests to Telegram API.
  * It is not necessary for the students taking this course to understand the contents of this file.
  * 
  */
class AdvancedBot extends BasicBot:
  
  private var chatId: ChatId = _
  private var message: Message = _
  private var data: String = _
  private var messageId: Int = _


  type Button = InlineKeyboardButton
  type Message = com.bot4s.telegram.models.Message


  // Housekeeping: token, URLS, etc.
  
  def onLeaveMessage(action: User => Unit) = onMessage {
    implicit msg => Future {
      msg.leftChatMember.foreach {
        user => action(user)
      }
    }
  }


  /**
    * Creates a message with a so called inline keyboard, i.e. a keyboard embedded to the message.
    *
    * @param command      The name of the command
    * @param replyMessage The textual message accompanying the keyboard
    * @param keyboard     The keyboard as a two-dimensional sequence of buttons. The first sequence represents rows,
    *                     the inner sequence represents columns within rows.
    * @return
    */
  def onCommandWithInlineKeyboard(command: String, replyMessage: String, keyboard: Seq[Seq[Button]]) = {
    onCommand(command) { implicit msg =>
      val kb = InlineKeyboardMarkup.apply(keyboard)
      request(SendMessage(ChatId.fromChat(msg.chat.id), replyMessage, replyMarkup = Some(kb), parseMode = Some(ParseMode.HTML))).map(_ => ())
    }

  }

  /**
    * Creates a new button that takes the user the specified URL when pressed
    *
    * @param text The text on the button
    * @param url  The URL the button links to
    * @return A button with the given text and URL
    */
  def createURLButton(text: String, url: String): Button = InlineKeyboardButton.url(text, url)

  /**
    * Creates a button, the pressing of which, causes a change in the keyboard.
    * @param text The text on the button
    * @param data The data the button sends upon being pressed. This can be used to e.g. determine, which button
    *             was pressed if there are many
    */
  def createActiveButton(text: String, data: String): Button = InlineKeyboardButton.callbackData(text, data)


  /**
    * Listens to and handles so-called callback queries originating from clicks on active buttons.
    * @param action A method taking a string parameter (the data in the callback query) and does something with it.
    * @return
    */
  def onCallback(action: String => Any) = {
    onCallbackQuery { implicit cbq =>
      // Saving data related to the message for when we need to send the user something back.
        data      = cbq.data.get
        message   = cbq.message.get
        chatId    = ChatId.fromChat(message.chat.id)
        messageId = message.messageId

        Future(action(data))
    }
  }

  /**
    * Updates the keyboard with the given keyboard layout
    * @param keyboard The layout of the new keyboard as a two-dimesional collection of buttons.
    * @return
    */

  def updateKeyboard(keyboard: Seq[Seq[Button]]) = {
    request(EditMessageReplyMarkup(chatId = Some(chatId), messageId = Some(messageId), replyMarkup = Some(InlineKeyboardMarkup.apply(keyboard))))

    }

  def updateMessage(newText: String) = {
    request(methods.EditMessageText(Some(chatId), Some(messageId), text = newText))
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
  

  // Use writeMessage
  def respond(text: String) = {
    request(SendMessage(chatId, text, parseMode = Some(ParseMode.HTML)))
  }

  /**
    * A built-in kill switch system. Disable it if needed by overriding killSwitch
    */
 
  onUserCommand("kill", message => killSwitch())

end AdvancedBot
