package s1.telegrambots

import java.util.Scanner
import java.io.File
import scala.collection.mutable.Buffer
import scala.io.StdIn.readLine

//DTSTART:
//DTEND:
//DTSTART;VALUE=DATE
//DTEND;VALUE=DATE
//BEGIN:VEVENT
//END:VEVENT


object FileHandler {


  @main def printCreatedEvents()=
    val events=eventsFromICSFile(File("C:/Users/Jarvi/Desktop/aleksi.kuusinen03@gmail.com.ical/aleksi.kuusinen03@gmail.com.ics/"))
    val sevents=eventsFromICSFile(File("C:/Users/Jarvi/Desktop/aleksi.kuusinen03@gmail.com.ical/74e92f28-ed72-451d-9185-3874c05029e2.ics"))
    refs/remotes/origin/main
    println("Event buffer size: " + events.size)
    println("School event buffer size: " + sevents.size)
    val c =Calendar(events,12)
    println(s"\n Sorted calendar: \n")
    c.sortEventsByStartTime
    c.eventList.foreach(println)

    val d =Calendar(sevents,12)
    val f=c.fuseTwoCalendars(d)
    println("FUSED CALENDARS")

    f.sortEventsByStartTime()
    f.printList()
    /*println(s"\n Sorted calendars: \n")
    println("1. :")
    c.sortEventsByStartTime()
    c.printList()
    println("2. :")
    d.sortEventsByStartTime()
    d.printList()*/


      refs/remotes/origin/main
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
   * @param file The .ics-file which contains event data.
   * @return a Buffer[CalendarEvent] object which contains all events from the file.
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
                eventList+=CalendarEvent(tempBuffer.head,tempBuffer(1))
                tempBuffer.clear()    //Empty the temporary storage
          }
          //If the time is a starting time and belongs to and event, insert it on index 0. File contains two syntax variations, therefore two cases
          case "DTSTART"  =>{
            if inEvent then
              tempBuffer.insert(0,line.split(":")(1))
          }
          /*case "DTSTART;VALUE=DATE" =>{
            if inEvent then
              tempBuffer.insert(0,line.split(":")(1))
          }*/
          //If the time is an ending time and belongs to and event, insert it on index 1. File contains two syntax variations, therefore two cases
          case "DTEND" =>{
            if inEvent then
              tempBuffer.insert(1,line.split(":")(1))
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
              tempBuffer.insert(1,returnString)

          }

          /*case "DTEND;VALUE=DATE" =>{
            if inEvent then
              tempBuffer.insert(1,line.split(":")(1))
          }*/
          case _ =>
      catch
        case e: Exception => {
          e.printStackTrace()
        }
    eventList
}
