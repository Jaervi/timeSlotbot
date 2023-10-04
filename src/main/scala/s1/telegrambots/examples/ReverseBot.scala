package s1.telegrambots.examples
import com.bot4s.telegram.models
import s1.telegrambots.BasicBot

/**
  * Ärsyttävä Botti joka kääntää sanat nurinpäin
  *
  * Botti saamaan reagoimaan kanavan tapahtumiin luomalla funktio/metodi joka käsittelee
  * halutuntyyppistä dataa ja joko palauttaa merkkijonon tai tekee jotain muuta saamallaan
  * datalla.
  *
  * Alla on yksinkertainen esimerkki - metodi joka ottaa merkkijonon ja kääntää sen nurin.
  * Luokassa BasicBot on joukko metodeja, joilla voit asettaa botin suorittamaan oman metodisi
  * ja tekemään tiedolla jotain. replyToString rekisteröi bottiin funktion joka saa
  * syötteekseen jokaisen kanavalle kirjoitetun merkkijonon. Se, mitä funktio
  * palauttaa lähetetään kanavalle.
  */
object ReverseBot extends App:
    object Bot extends BasicBot:
        /**
         * Kääntää sanan toisin päin
         */
        def nurinpain(s: String) = 
            val r = s.reverse
            println(s + "-->" + r) // Tulostaa saman konsoliin
            s.reverse
    
        /**Metodi, joka palauttaa aina viestin "Moikka!!" parametreista huolimatta.*/
        def tervehdys(s: Message) = "Moikka!!"


        /**
         * rekisteröi botille uuden toiminnon joka ajetaan aina kun
         * kanavalle kirjoitetaan. Kirjaimellisesti vastaa siis tekstin pätkään.
         * Nyt botti kutsuu funktiota nurinpäin antaen sille parametriksi lähettämäsi viestin vastaten kyseisen funktion palautuksella.
         */
        this.onUserMessageReplyString(nurinpain)

        /**
         * Botin voi käynnistää /start komennolla.
         * Tällöin botti ajaa tervehdys -metodin
         */
        this.onUserCommand("start", tervehdys)

        // Lopuksi Botti pitää vielä saada käyntiin
        this.run()
        // Tarkistetaan, että lähti käyntiin
        println("Started the bot")
    end Bot
    // Tämä rivi pyytää yllä olevan objektin, joka ajaa sen.
    val bot = Bot 
end ReverseBot

