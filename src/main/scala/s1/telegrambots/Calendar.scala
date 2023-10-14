package s1.telegrambots

import java.text.SimpleDateFormat
import java.time.*
import java.util.Date
import java.time.LocalDate
import scala.collection.mutable.Buffer

// TODO: Make code clearer and improve commenting.

/**
 * Represents a calendar, storing data about events. Mainly used as an interface to perform operations between different event lists.
 * @param events A Buffer containing events to be included in this calendar. The buffer is saved as a var-variable since you can add events manually afterwards.
 * @param timeCreated A simple number used for determining which of two calendars is newer or removing old calendars after a certain time period.
 */
class Calendar(events:Buffer[CalendarEvent], val timeCreated:Long):

  var eventList=events
  filterForCurrentTime()
  sortEventsByStartTime()
  removeDayEvents()

  /**
   * A simple method for getting the current time in minutes (since year 0).
   *
   * @return The current time in minutes as Int
   */
  private def currentTimeInMinutes: Int = {
    (LocalDate.now().getYear) * 525600 +
    (LocalDate.now().getMonthValue) * 43200 + (LocalDate.now().getDayOfMonth) * 1440 +
    (LocalTime.now().getHour) * 60 + LocalTime.now().getMinute
  }

  //Some methods for manipulating the eventList variable. Includes methods for ordering elements and for removing unwanted elements
  /** Removes all events before the present */
  def filterForCurrentTime()=
    eventList=eventList.filter(_.startTimeInMinutes>=this.currentTimeInMinutes)
    /** Sorts the list by event starting time */
  def sortEventsByStartTime()=
    eventList=eventList.sortBy(_.startTimeInMinutes)
    /** Removes all events which start the specified amount of days after the current time */
  def limitEventsByDays(days:Int)=
    eventList=eventList.filter(_.startTimeInMinutes<=currentTimeInMinutes+days*1440)
    /** Removes all events lasting a whole day (they are not used in making the empty slots) */
  def removeDayEvents()=
    eventList=eventList.filter(_.startTime.length>8)
    /** Removes all events that are "covered" by another event. More accurate definition found at CalendarEvent.scala -> covers()*/
  def removeCoveredEvents()=
    val tempBuffer = Buffer[CalendarEvent]()
    for event <- eventList do 
      if eventList.forall(!_.covers(event)) then
        tempBuffer.append(event)
    eventList=tempBuffer

  /**Prints the entire event list*/
  def printList()=eventList.foreach(println)

  /**Makes a combination of two different calendars into one calendar*/
  def fuseTwoCalendars(calendar:Calendar):Calendar=
    var combinedBuffer=(calendar.eventList ++ (this.eventList)).sortBy(_.startTimeInMinutes)
    Calendar(combinedBuffer,timeCreated)
    /*var endSortedCombinedBuffer=(fuseBuffer ++ (this.eventList)).sortBy(_.endTimeInMinutes)
    var newEventBuffer=Buffer[CalendarEvent]()
    eventList.foreach(e=>fuseBuffer.filter(!e.covers(_)))
    fuseBuffer.foreach(fe=>eventList.filter(!fe.covers(_)))
    for event <- this.eventList do
      if combinedBuffer.forall(e=>e.startTimeInMinutes>=event.endTimeInMinutes || e.endTimeInMinutes<=event.startTimeInMinutes) then
        newEventBuffer.append(event)
    for fuseEvent <- fuseBuffer do
      if combinedBuffer.forall(e=>e.startTimeInMinutes>=fuseEvent.endTimeInMinutes || e.endTimeInMinutes<=fuseEvent.startTimeInMinutes) then
        newEventBuffer.append(fuseEvent)
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
    Calendar(newEventBuffer,timeCreated)*/

  /**
   * Calculates all the empty time slots from the specified calendar.
   * @param calendar The calendar from which empty slots are fetched
   * @param slotLength Specifies the minimum length of an empty spot in minutes
   * @param dayLimit Limits the number of days looked forward in days
   * @return A Calendar which has the empty slots as its events
   */
  def fetchEmptySlots(calendar:Calendar,slotLength:Int,dayLimit:Int):Calendar=
    var emptySlotBuffer=Buffer[CalendarEvent]()
    calendar.filterForCurrentTime()
    calendar.sortEventsByStartTime()
    calendar.limitEventsByDays(dayLimit)
    val eventBuffer=calendar.eventList
    if (eventBuffer(1).startTimeInMinutes-eventBuffer(0).endTimeInMinutes >=slotLength) then
      emptySlotBuffer.append(CalendarEvent(eventBuffer(0).endTime,eventBuffer(1).startTime))
    for i <- 2 until eventBuffer.size do
      if (eventBuffer(i).startTimeInMinutes-eventBuffer(i-1).endTimeInMinutes >=slotLength) && !eventBuffer(i-2).covers(eventBuffer(i-1)) then
        emptySlotBuffer.append(CalendarEvent(eventBuffer(i-1).endTime,eventBuffer(i).startTime))
    Calendar(emptySlotBuffer,12)

  /**
   * Adds an event to the calendar. Used for manual event creation.
   * @param startTime The starting time for the event. Must be in the format yyyyMMddhhmm
   * @param endTime The ending time for the event. Must be in the format yyyyMMddhhmm
   */
  def addEvent(startTime:String,endTime:String)=
    val startString=startTime.substring(0,8)+"T"+startTime.substring(8,12)+"00Z"
    val endString=endTime.substring(0,8)+"T"+endTime.substring(8,12)+"00Z"
    eventList.append(CalendarEvent(startString,endString))
    sortEventsByStartTime()

  /**
   * Creates events to prevent night-time for showing as a free time slot. Java.util.Calendar is used to make calculating subsequent days easier
   * @param eveningLimit The evening limit as Int. Empty slots do not last after this time
   * @param morningLimit The morning limit as Int. Empty slots do not start before this time
   * @param days Is used to specify the length of how many elements are created (a heuristic, can be defined as wanted)
   */
  //
  //Limit parameters define the evening and morning limits and parameter days is used to specify the length of how many elements are created.
  //
  def addNightLimits(eveningLimit:Int,morningLimit:Int,days:Int)=
    var morningString=(morningLimit-3).toString
    var eveningString=(eveningLimit-3).toString
    if morningLimit<13 then
      morningString="0"+morningString
    if eveningLimit<13 then
      eveningString="0"+eveningString
    val format = SimpleDateFormat("yyyyMMdd")
    val calendar = java.util.Calendar.getInstance()
    calendar.setTime(format.parse(s"${LocalDate.now().getYear.toString}${LocalDate.now().getMonthValue.toString}${LocalDate.now().getDayOfMonth.toString}"))
    for i <- 0 until days do
      val start=s"${format.format(calendar.getTime)}${eveningString}00"
      calendar.add(java.util.Calendar.DAY_OF_MONTH,1)
      val end=s"${format.format(calendar.getTime)}${morningString}00"
      addEvent(start,end)
    sortEventsByStartTime()
        /*if (eventBuffer(i+1).startTimeInMinutes-eventBuffer(i).endTimeInMinutes >=slotLength
          && (eventBuffer(i).endHour*60+eventBuffer(i).endMinute <22*60-slotLength || eventBuffer(i+1).startHour*60+eventBuffer(i+1).startMinute>8*60+slotLength)) then

        val beginStamp="080000Z"
        val beginTime=8*60
        val finalStamp="220000Z"
        val finalTime=22*60
        var morningTime=(LocalDate.now().getYear)*525600+(LocalDate.now().getMonthValue)*43200+(LocalDate.now().getDayOfMonth+1)*1440+morningLimit*60
        var eveningTime=(LocalDate.now().getYear)*525600+(LocalDate.now().getMonthValue)*43200+(LocalDate.now().getDayOfMonth)*1440+eveningLimit*60*/
        //val nextDayAtEight=(LocalDate.now().getYear)*525600+(LocalDate.now().getMonthValue)*43200+(LocalDate.now().getDayOfMonth+1)*1440+8*60





