package s1.telegrambots

import java.util.Scanner
import java.io.File
import scala.collection.mutable.Buffer
import scala.io.StdIn.readLine

//DTSTART:
//DTEND:
//DTSTART;VALUE=DATE:
//DTEND;VALUE=DATE:
//BEGIN:VEVENT
//END:VEVENT


object FileHandler {


  @main def printCreatedEvents()=
    println("Test method not initialized")
    /*val events=eventsFromICSFile("C:/Users/Aleksi/Desktop/aleksi.kuusinen03@gmail.com.ical/aleksi.kuusinen03@gmail.com.ics")
    println("Event buffer size: " + events.size)
    val c =Calendar(events,12)
    println(s"\n Sorted calendar: \n")
    c.sortEventsByStartTime()
    c.eventList.foreach(println)*/

    /*while true do
      val input=readLine("Enter an index: ")
      val e = events(input.toInt)
      //c.sortEventsByTime
      println(e.toString)
      println("Duration: " + events(input.toInt).duration)
      println(e.startYear)
      println("SD: " +e.startDay)
      println("SM: " +e.startMonth)
      println("ED: " +e.endDay)
      println("EM: " +e.endMonth)
      println("SHr: " +e.startHour)
      println("EHr: " +e.endHour)
      println("SMin: " +e.startMinute)
      println("EMin: " +e.endMinute)*/
    /*for e <- events do
      println(e.startYear)
      println("SD: " +e.startDay)
      println("SM: " +e.startMonth)
      println("ED: " +e.endDay)
      println("EM: " +e.endMonth)*/

  /**
   * Creates a Buffer containing all the events from the specified .ics-file. Events are stored in CalendarEvent format in the Buffer.
   * @param filePath The .ics-file which contains event data.
   * @return Returns a Buffer[CalendarEvent] object which contains all events from the file.
   */
  def eventsFromICSFile(file:File)=
    //Creating variables for file-reading
    //val file = File(filePath)
    val scanner = Scanner(file)
    //Creating two lists, eventList being the list which is updated and eventually given as the return parameter. TempBuffer serves as a temporary storage for each iteration.
    var eventList=Buffer[CalendarEvent]()
    var tempBuffer=Buffer[String]()
    //A boolean variable for extracting information only from events
    var inEvent = false

    while (scanner.hasNextLine) do
      try
        var line = scanner.nextLine()
        //A check for avoiding index errors for glitched/otherwise incomplete lines
        if line.split(":").length <2 then
          line="NULL:NULL"
        line.split(":")(0) match
          case "BEGIN" => inEvent=true
          case "END" => {
            if inEvent then
              inEvent=false
              if tempBuffer.size>1 then
                eventList+=CalendarEvent(tempBuffer(0),tempBuffer(1))
                tempBuffer.clear()    //Empty the temporary storage
          }
          //If the time is a starting time and belongs to and event, insert it on index 0. File contains two syntax variations, therefore two cases
          case "DTSTART"  =>{
            if inEvent then
              tempBuffer.insert(0,line.split(":")(1))
          }
          case "DTSTART;VALUE=DATE" =>{
            if inEvent then
              tempBuffer.insert(0,line.split(":")(1))
          }
          //If the time is an ending time and belongs to and event, insert it on index 1. File contains two syntax variations, therefore two cases
          case "DTEND" =>{
            if inEvent then
              tempBuffer.insert(1,line.split(":")(1))
          }
          case "DTEND;VALUE=DATE" =>{
            if inEvent then
              tempBuffer.insert(1,line.split(":")(1))
          }
          case _ =>
      catch
        case e: Exception => {
          e.printStackTrace()
        }
    eventList
}
