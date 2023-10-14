package s1.telegrambots
import com.bot4s.telegram.models.Message
import com.bot4s.telegram.models.ChatType
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

import java.net.URL
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.Breaks.{break, breakable}

object FilePreprocessor {
  private var filepathBufferMap = new HashMap[Long,Buffer[String]]()

  /**
   * Finds files from message that contain documents or links and adds them to a hashmap containing
   * a buffer of filepath URLs for each user. Note that URLs to documents sent as attachments are only
   * valid for one hour after sending and then must be handled again.
   * @param msg Telegram message
   */
  def parseFilepathsFromMessage(msg: Message): Unit =
    // Only get filepaths from messages sent in private chats
    if (msg.chat.`type` != ChatType.Private) then
      return ()
    end if

    var filepaths: Buffer[String] = Buffer()
    // Find sender user ID
    val sender: Long =
      msg.from match
        case Some(user) =>
          println(user.id.toString)
          user.id
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
      case None =>

    // The message contains 'entities'; lets see if it contains URLs and use those as filepaths
    msg.entities match
      case Some(entities) =>
        // Lets loop through all entities in the message and see if they contain an URL
        for (i <- entities.indices)
          entities(i).url match
            case Some(url) => filepaths += url
            case None =>
      case None =>

    // Add found filepaths to hashmap waiting for processing
    if (filepathBufferMap.contains(sender)) then
      filepathBufferMap(sender) = filepathBufferMap(sender) ++ filepaths
    else
      filepathBufferMap += (sender, filepaths)

    println(s"Files found: ${filepaths.toString()}")
  end parseFilepathsFromMessage

  /**
   * Get calendar ICS file of specific user
   * @param userid Unique identifier of a user
   * @return File wrapped in Option. None if user hasn't sent any files
   */
  def getFile(userid: Long): Option[File] =
    // Case 1. There are new files waiting in filepathBuffermap
    if (filepathBufferMap.contains(userid)) then
      if (filepathBufferMap(userid).nonEmpty) then
        val fileopt: Option[File] = fuseFiles(userid, filepathBufferMap(userid))
        // Were files of acceptable type; if not, we will continue further
        if fileopt.isDefined then
          filepathBufferMap.remove(userid)
          return fileopt
        end if
      end if
      // This user has been processed from filepathBufferMap
      filepathBufferMap.remove(userid)
    end if

    // Case 2. There are no new/acceptable files waiting in filepathBuffermap; lets check if there is a previous file
    var file = File(s"${System.getProperty("user.dir")}${File.separator}Calendars${File.separator}${userid.toString}.ICS")
    val threeDays: Long = 259200000
    if (file.exists()) then
      if (file.lastModified() > (java.util.Calendar.getInstance().getTimeInMillis - threeDays)) then
        Some(file)
      else
        None
      end if
    // Case 3. There are no new files waiting in filepathBuffermap and there is no existing file in Calendars
    else
      None


  end getFile

  /**
   * Gets data from all files defined in filepathBuffer and fuses them together into one ICS file.
   * @param filepathBuffer Buffer of strings, where each element is a URL of a file
   * @return Fused file wrapped in option. None if none of the files specified in filepathBuffer is of acceptable type
   */
  private def fuseFiles(userid: Long, filepathBuffer: Buffer[String]): Option[File] =
    // Create new file by user id to Calendars folder in project directory, and name the file <userid>.ICS
    var outputFile: File = new File(s"${System.getProperty("user.dir")}${File.separator}Calendars${File.separator}${userid.toString}.ICS")
    var outputStream: FileWriter = new FileWriter(outputFile)

    var isFirst: Boolean = true // Is current file the first acceptable file in filepathBuffer?
    // Lets try to parse a ICS file from each filepath
    for (i <- filepathBuffer.indices)
      try
        // Open file stream via URL, catch possible errors
        // TODO: Currently only handles URLs that end in .ICS (are direct links) but not all links are...
        var inputStream: BufferedReader = new BufferedReader(new InputStreamReader(new URL(filepathBuffer(i)).openStream()))

        // These will be assigned different values in while loop
        var acceptableFile: Boolean = false
        var isHeader: Boolean = true
        var line: String = ""
        breakable {
          while(true)
            // Check if current line is null, that means end of file
            line = inputStream.readLine()
            if (line == null) then break()

             // Check if this is the first line
            if (!acceptableFile) then
              if (!line.contains("BEGIN:VCALENDAR")) then
                break() // File does not start with BEGIN:VCALENDAR so it is not iCalendar file
              else
                if (isFirst) then
                  outputStream.write(line + "\r\n")
                acceptableFile = true
            else
              // Everything is header before first BEGIN:VEVENT
              if (isHeader && line.contains("BEGIN:VEVENT")) then
                isHeader = false
                isFirst = false // End of header text has been reached
              if (isHeader) then
                // If this is the first document and its header, write header to file
                if (isFirst) then
                  outputStream.write(line + "\r\n")
              // Everything after first BEGIN:VEVENT will be written to file always
              else
                outputStream.write(line + "\r\n")
        }
        // Close file that was being read
        inputStream.close()
      catch
        case error: FileNotFoundException => println(s"File ${filepathBuffer(i)} not found")
        case error: Exception => println(s"Not able to read file at ${filepathBuffer(i)}")

    // Close output file
    outputStream.close()

    // If isFirst is true, that means no acceptable files were successfully merged and the created file is empty
    if (isFirst)
      Some(outputFile)
    else
      Some(outputFile)
  end fuseFiles

  /**
   * Returns amount of files that are pending for this user
   * @param userid userid long integer
   * @return Amount of files pending, 0 if no files but name is on list and -1 if name not on list
   */
  def isPending(userid: Long): Int =
    if (filepathBufferMap.contains(userid)) then
      filepathBufferMap(userid).size
    else
      -1
  end isPending


}
