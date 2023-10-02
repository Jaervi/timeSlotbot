package s1.telegrambots.examples

import s1.telegrambots.BasicBot

/**
  * Tervehdysbotti, joka tervehtii kanavalle tulijoita
  *
  * Botti saamaan reagoimaan kanavan tapahtumiin luomalla funktio/metodi joka käsittelee
  * halutuntyyppistä dataa ja joko palauttaa merkkijonon tai tekee jotain muuta saamallaan
  * datalla.
  *
  */
object AlarmBot extends App:

  object Bot extends BasicBot:

      /**
        * Viesti käyttäjälle toivotun ajan kuluttua
        */
      def ajastus(message: Message): String = {
        // Muutetaan merkkijonomme Option-käärityksi numeroksi
        // jos tekstiä ei voi muuttaa numeroksi saadaan None.
        val aika = util.Try(getString(message).toInt).toOption

        aika match {
          case None => "Et antanut numeroa"
          case Some(sekunnit) => {

            // Laitetaan rinnakkainen työtehtävä käyntiin
            scala.concurrent.Future {
              // Painetaan mieleen mistä chatistä hälytys pyydettiin
              val chat = getChatId(message)
              // Odotellaan hieman
              Thread.sleep(sekunnit * 1000)
              // Ja kirjoitellaan sinne hälytys
              writeMessage("Hälytys!!!", chat)
            }

            // Kanavalle voidaan kuitenkin viestiä heti
            "Ajastettiin hälytys " + sekunnit + " kuluttua."
          }
        }
      }

      /**
        * rekisteröi botille uuden toiminnon joka ajetaan kun
        * käyttäjä kirjoittaa telegramiin esim. /ajasta 10
        */
      this.onUserCommand("ajasta", ajastus)

      // Lopuksi Botti pitää vielä saada käyntiin
      this.run()

      println("Started")

  end Bot
  val bot = Bot
end AlarmBot