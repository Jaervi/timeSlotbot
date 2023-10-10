package s1.telegrambots

import java.time.*
import java.util.Date
import java.time.LocalDate
import scala.collection.mutable.Buffer

class Calendar(events:Buffer[CalendarEvent], val timeCreated:Int):

  var eventList=events
  private val currentTimeInMinutes={
    (LocalDate.now().getYear)*525600+
    (LocalDate.now().getMonthValue)*43200+(LocalDate.now().getDayOfMonth)*1440+
    (LocalTime.now().getHour)*60+LocalTime.now().getMinute
  }

  def filterForCurrentTime()=
    eventList=eventList.filter(_.startTimeInMinutes>=this.currentTimeInMinutes)
  def sortEventsByStartTime()=
    eventList=eventList.sortBy(_.startTimeInMinutes)
  def limitEventsByDays(days:Int)=
    eventList=eventList.filter(_.startTimeInMinutes<=currentTimeInMinutes+days*1440)

  def printList()=eventList.foreach(println)

  def fuseTwoCalendars(calendar:Calendar)=
    var fuseBuffer=calendar.eventList
    var endSortedFuseBuffer=fuseBuffer.sortBy(_.endTimeInMinutes)
    var combinedBuffer=(fuseBuffer ++ (this.eventList)).sortBy(_.startTimeInMinutes)
    var endSortedCombinedBuffer=(fuseBuffer ++ (this.eventList)).sortBy(_.endTimeInMinutes)
    var newEventBuffer=Buffer[CalendarEvent]()
    eventList.foreach(e=>fuseBuffer.filter(!e.covers(_)))
    fuseBuffer.foreach(fe=>eventList.filter(!fe.covers(_)))
    /*for event <- this.eventList do
      if combinedBuffer.forall(e=>e.startTimeInMinutes>=event.endTimeInMinutes || e.endTimeInMinutes<=event.startTimeInMinutes) then
        newEventBuffer.append(event)
    for fuseEvent <- fuseBuffer do
      if combinedBuffer.forall(e=>e.startTimeInMinutes>=fuseEvent.endTimeInMinutes || e.endTimeInMinutes<=fuseEvent.startTimeInMinutes) then
        newEventBuffer.append(fuseEvent)*/
    println(combinedBuffer.size)
    for event <- this.eventList do
      var baseEvent=event
      for fuseEvent <- combinedBuffer do
        if fuseEvent.startsEarlierThan(baseEvent) && fuseEvent.endsDuring(baseEvent) then
          baseEvent.startTime=fuseEvent.startTime
      for fuseEvent <-endSortedCombinedBuffer do
        if fuseEvent.endsLaterThan(baseEvent)&& fuseEvent.startsDuring(baseEvent) then
          baseEvent.endTime=fuseEvent.endTime
      if newEventBuffer.forall(!baseEvent.existsDuring(_)) then
        newEventBuffer.append(baseEvent)
    Calendar(newEventBuffer,timeCreated)


