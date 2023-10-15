package s1.telegrambots

import java.util.Scanner
import java.io.File
import scala.collection.mutable.Buffer
import scala.io.StdIn.readLine

object FileHandler {

  /**
   * Creates a Buffer containing all the events from the specified .ics-file. Events are stored in CalendarEvent format in the Buffer.
   * @param file The .ics-file which contains event data.
   * @return a Buffer[CalendarEvent] object which contains all events from the file.
   */
  def eventsFromICSFile(file:File)=
    //Creating variables for file-reading
    val scanner = Scanner(file)
    //Creating two lists, eventList being the list which is updated and eventually given as the return parameter. TempBuffer serves as a temporary storage for each iteration.
    var eventList=Buffer[CalendarEvent]()
    var tempBuffer=Buffer[String]("","")
    //A boolean variable for extracting information only from events
    var inEvent = false

    while (scanner.hasNextLine) do
      try
        var line = scanner.nextLine()
        //A check for avoiding index errors for glitched/otherwise incomplete lines
        if line.split(":").length <2 then
          line="NULL:NULL"
        line.split(":")(0) match
          case "BEGIN" =>
            if (line.split(":")(1).contains("VEVENT")) then
              inEvent = true
          case "END" => {
            if (line.split(":")(1).contains("VEVENT")) && inEvent then
              inEvent=false
              if tempBuffer(0) != "" && tempBuffer(1) != "" then
                eventList+=CalendarEvent(tempBuffer.head,tempBuffer(1))
                tempBuffer = Buffer("","")    //Empty the temporary storage
          }
          //If the time is a starting time and belongs to and event, insert it on index 0.
          case "DTSTART"  =>{
            if inEvent then
              tempBuffer(0) = (line.split(":")(1))
          }
          //If the time is an ending time and belongs to and event, insert it on index 1.
          case "DTEND" =>{
            if inEvent then
              tempBuffer(1) = (line.split(":")(1))
          }
          //Certain files have a syntax with a start time and duration. A case for calculating the ending time for that alternative
          case "DURATION" =>{
            if inEvent then
              var startHour=tempBuffer.head.substring(9,11).toInt
              var startMin=tempBuffer.head.substring(11,13).toInt
              var tempString = line.split(":")(1).substring(2,line.split(":")(1).length)
              tempString=tempString.substring(0,tempString.length-1)
              val hourAndMin=tempString.split("H")
              if hourAndMin.size>1 then
                startHour+=hourAndMin(0).toInt+(hourAndMin(1).toInt+startMin)/60
                startMin=(hourAndMin(1).toInt+startMin)%60
              else
                startHour+=hourAndMin(0).toInt
              var startHourString=""
              var startMinString=""
              if startHour<10 then
                startHourString = s"0$startHour"
              else
                startHourString = s"$startHour"
              if startMin<10 then
                startMinString = s"0$startMin"
              else
                startMinString = s"$startMin"
              val returnString=s"${tempBuffer.head.substring(0,9)}${startHourString}${startMinString}00Z"
              tempBuffer(1) = (returnString)

          }
          //Certain lines have a different syntax for example with a timezone. This checks for those edge cases.
          case _ =>
            if (line.split(":")(0).contains("DTSTART")) && inEvent then
              tempBuffer(0) = (line.split(":")(1))
            else if (line.split(":")(0).contains("DTEND")) && inEvent then
              tempBuffer(1) = (line.split(":")(1))
      catch
        case e: Exception => {
          e.printStackTrace()
        }
    eventList
}
