package s1.telegrambots.examples

import s1.telegrambots.BasicBot

/**
  * Photobot etsii sanaa ja lähettää löytäessään haluamans kanavalle kuvan
  */
object PhotoBot extends App:
    object Bot extends BasicBot:

      private val pic        = "kuva.png"
      private val wordToFind = "pepper"

      /**
        * Botti vastaa kuvalla löytäessään haluamansa viestin
        */
      def pepperReply(msg: Message) = 
        val id = getChatId(msg)
        val ms = getString(msg)
        println(ms)
        if ms.contains(wordToFind) then
          sendPhoto(pic, id)
          writeMessage(wordToFind + " found!", id)
        else
          writeMessage("try again", id)
          

      end pepperReply

      // Rekisteröidään bat-metodi ajettavaksi aina kun kanavalle kirjoitetaan
      this.onUserMessage(pepperReply)

      // Lopuksi Botti pitää vielä saada käyntiin
      this.run()

      println("Botti käynnissä")
    end Bot
    val bot = Bot

end PhotoBot
