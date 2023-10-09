package s1.telegrambots
import com.bot4s.telegram.models.Message
import s1.telegrambots.BasicBot
import s1.telegrambots.YourBot.Bot.token
import scalaj.http.Http

import scala.collection.mutable
import scala.collection.mutable.HashMap
import scala.collection.mutable.Buffer
import scala.util.Using
import scala.io.Source
import java.io.*
import sttp.client3.{Response, SttpBackend, quickRequest}
import sttp.client3.quick.*
import sttp.client3.*

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object FilePreprocessor {
  private var filepathBufferMap = new HashMap[Long,Buffer[String]]()

  /**
   * Finds files from message that contain documents or links and adds them to a hashmap containing
   * a buffer of filepath URLs for each user. Note that URLs to documents sent as attachments are only
   * valid for one hour after sending and then must be handled again.
   * @param msg Telegram message
   */
  def getFilepathsFromMessage(msg: Message): Unit =
    var filepaths: Buffer[String] = Buffer()
    // Find sender user ID
    val sender: Long =
      msg.from match
        case Some(user) => user.id
        case None => return () // No user id = message wasnt sent by user so no need to process

    // The file containts a document; lets parse filepath using direct calls to Telegram API
    msg.document match
      case Some(document) =>
        // Create HTTP request to Telegram API to receive file path to be able to download file
        val fileId = document.fileId
        val httpBackend = HttpURLConnectionBackend()
        val request = quickRequest.get(uri"https://api.telegram.org/bot$token/getFile?file_id=$fileId")
        val response: Response[String] = request.send(httpBackend)

        // We have no actual JSON parser so lets find file_path value by splitting the string and finding the correct one
        val jsonData = response.toString().split(',')
        for (i <- jsonData.indices)
          if (jsonData(i).contains("file_path")) then
            // This sets filepath to be something like base+"/documents/example.ics"
            filepaths += s"https://api.telegram.org/file/bot$token/${jsonData(i).split('"')(3)}"

    // The message contains 'entities'; lets see if it contains URLs and use those as filepaths
    msg.entities match
      case Some(entities) =>
        // Lets loop through all entities in the message and see if they contain an URL
        for (i <- entities.indices)
          entities(i).url match
            case Some(url) => filepaths += url

    // Add found filepaths to hashmap waiting for processing
    filepathBufferMap += (sender, filepaths)
  end getFilepathsFromMessage

  /**
   * Get calendar ICS file of specific user
   * @param userid Unique identifier of a user
   * @return File wrapped in Option
   */
  def getFile(userid: Long): Option[File] =
    filepathBufferMap.get(userid) match
      case Some(filepathBuffer) =>
        if filepathBuffer.isEmpty then
          None
        else
          fuseFiles(filepathBuffer)
      case None => None
  end getFile

  /**
   * Gets data from all files defined in filepathBuffer and fuses them together into one ICS file.
   * @param filepathBuffer Buffer of strings, where each element is a URL of a file
   * @return Fused file wrapped in option. None if none of the files specified in filepathBuffer is of acceptable type
   */
  private def fuseFiles(filepathBuffer: Buffer[String]): Option[File] = ???

}
