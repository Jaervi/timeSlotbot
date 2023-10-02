package s1.telegrambots.examples

import s1.telegrambots.BasicBot
import java.awt.image.BufferedImage

/**
  * Imagebot etsii sanaa ja lähettää, löytäessään haluamansa, kanavalle kuvan
  */
object ImageBot extends App:
    object Bot extends BasicBot:

      private val wordToFind = "picture"

      // The following line prevents the program from creating an application window when
      // graphics are handled
      System.setProperty("java.awt.headless", "true")

      /**
        * Botti vastaa kuvalla löytäessään haluamansa viestin
        */
      def imageReply(msg: Message) =
        val id = getChatId(msg)
        val ms = getString(msg)
        println(ms)
        if ms.contains(wordToFind) then
          val image    = BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB)
          val graphics = image.getGraphics.asInstanceOf[java.awt.Graphics2D]

          graphics.setColor(java.awt.Color.RED)
          graphics.fillRect(10, 10, 200, 200)

          sendBufferedImage(image, id)
          writeMessage(wordToFind + " found!", id)
        else
          writeMessage("try again", id)
          

      end imageReply

      // Rekisteröidään bat-metodi ajettavaksi aina kun kanavalle kirjoitetaan
      this.onUserMessage(imageReply)

      // Lopuksi Botti pitää vielä saada käyntiin
      this.run()

      println("Botti käynnissä")
    end Bot
    val bot = Bot

end ImageBot
