package s1.telegrambots

import java.text.SimpleDateFormat
import java.time.*
import java.util.Date
import java.time.LocalDate
import scala.collection.mutable.Buffer

// TODO: Make code clearer and improve commenting.
class Calendar(events:Buffer[CalendarEvent], val timeCreated:Int):

  var eventList=events
  private val currentTimeInMinutes={
    (LocalDate.now().getYear)*525600+
    (LocalDate.now().getMonthValue)*43200+(LocalDate.now().getDayOfMonth)*1440+
    (LocalTime.now().getHour)*60+LocalTime.now().getMinute
  }

  //Some methods for manipulating the eventList variable. Includes methods for ordering elements and for removing unwanted elements
  def filterForCurrentTime()=
    eventList=eventList.filter(_.startTimeInMinutes>=this.currentTimeInMinutes)
  def sortEventsByStartTime()=
    eventList=eventList.sortBy(_.startTimeInMinutes)
  def limitEventsByDays(days:Int)=
    eventList=eventList.filter(_.startTimeInMinutes<=currentTimeInMinutes+days*1440)
  def removeDayEvents()=
    eventList=eventList.filter(_.startTime.length>8)

  //Prints the entire event list
  def printList()=eventList.foreach(println)

  //Makes a combination of two different calendars into one calendar
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

  //Calculates all the empty time slots from the specified calendar.
  //Parameter slotLength specifies the minimum length of an empty spot and dayLimit limits the number of days looked forward.
  //Returns a Calendar object which has the empty slots as events
  def fetchEmptySlots(calendar:Calendar,slotLength:Int,dayLimit:Int):Calendar=
    var emptySlotBuffer=Buffer[CalendarEvent]()
    calendar.filterForCurrentTime()
    calendar.sortEventsByStartTime()
    calendar.limitEventsByDays(dayLimit)
    val eventBuffer=calendar.eventList
    for i <- 0 until eventBuffer.size-1 do
      if (eventBuffer(i+1).startTimeInMinutes-eventBuffer(i).endTimeInMinutes >=slotLength) then
        emptySlotBuffer.append(CalendarEvent(eventBuffer(i).endTime,eventBuffer(i+1).startTime))
    Calendar(emptySlotBuffer,12)

  //Adds an event to the calendar. Used for manual event creation
  //Strings must be in the format yyyyMMddhhmm
  def addEvent(startTime:String,endTime:String)=
    val startString=startTime.substring(0,8)+"T"+startTime.substring(8,12)+"00Z"
    val endString=endTime.substring(0,8)+"T"+endTime.substring(8,12)+"00Z"
    eventList.append(CalendarEvent(startString,endString))
    sortEventsByStartTime()

  //Creates events to prevent night-time for showing as a free time slot.
  //Limit parameters define the evening and morning limits and parameter days is used to specify the length of how many elements are created.
  //java.util.Calendar library is used to make calculating subsequent days easier
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





