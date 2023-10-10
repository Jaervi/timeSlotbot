package s1.telegrambots

import scala.collection.mutable.Buffer

class Calendar(events:Buffer[CalendarEvent], val timeCreated:Int):

  var eventList=events


  def sortEventsByStartTime()=
    eventList=eventList.sortBy(_.startTimeInMinutes)

  def printList()=eventList.foreach(println)
