package s1.telegrambots

import java.text.SimpleDateFormat
import java.time.*

/*
Examples of lines including the date strings 
DTSTART:20180816T080000Z
DTEND:20180816T090000Z
*/

/**
 * Represents a single event with a starting time and an ending time.
 * @param startTime The starting time as a String. Is in format yyyyMMddThhmmssZ (this is due to the formatting in the .ics-files)
 * @param endTime The ending time as a String. Is in format yyyyMMddThhmmssZ (this is due to the formatting in the .ics-files)
 */
class CalendarEvent (var startTime:String,var endTime:String):
  
  /** Returns the duration of the event in minutes */
  def duration:Int=
    (endYear-startYear)*525600+(endMonth-startMonth)*43200+(endDay-startDay)*1440+(endHour-startHour)*60+(endMinute-startMinute)
  /**Returns the starting time in minutes as Int (from year 0)*/
  def startTimeInMinutes: Int=
    startYear*525600+startMonth*43200+startDay*1440+startHour*60+startMinute
  /**Returns the ending time in minutes as Int (from year 0)*/
  def endTimeInMinutes: Int=
    endYear*525600+endMonth*43200+endDay*1440+endHour*60+endMinute

  /**
   * Given a date in String format, determines if Daylight Savings Time is active on the specified date (in Helsinki)
   * @param eventDate A date in format yyyyMMdd as a String
   * @return True/false depending on the date
   */
  def isSummerTime(eventDate:String):Boolean=
    val format = SimpleDateFormat("yyyyMMdd")
    val date=format.parse(eventDate.substring(0,8))
    val now=ZonedDateTime.now(ZoneId.of("Europe/Helsinki"))
    val zoneRules=now.getZone.getRules
    zoneRules.isDaylightSavings(date.toInstant)



  //Defining variables for easier comparations with mathematical operations.
  def startYear:Int=startTime.substring(0,4).toInt
  def startMonth:Int=startTime.substring(4,6).toInt
  def startDay:Int=startTime.substring(6,8).toInt
  //Adding hours depending on DST
  def startHour:Int=
    if startTime.length>10 && isSummerTime(endTime) then
      startTime.substring(9,11).toInt+3
    else if startTime.length>10 && !isSummerTime(endTime) then
      startTime.substring(9,11).toInt+2
    else
      0
  def startMinute:Int=if startTime.length>10 then startTime.substring(11,13).toInt else 0

  def endYear:Int=endTime.substring(0,4).toInt
  def endMonth:Int=endTime.substring(4,6).toInt
  def endDay:Int=endTime.substring(6,8).toInt
  //Adding hours depending on DST
  def endHour:Int=
    if endTime.length>10 && isSummerTime(endTime) then
      endTime.substring(9,11).toInt+3
    else if endTime.length>10 && !isSummerTime(endTime) then
      endTime.substring(9,11).toInt+2
    else
      0
  def endMinute:Int=if endTime.length>10 then endTime.substring(11,13).toInt else 0

  //Various comparator methods used between two events
  /** Returns true if this event starts earlier or at the same time as the parameter event. */
  def startsEarlierThan(event:CalendarEvent): Boolean=
    this.startTimeInMinutes<=event.startTimeInMinutes
  /** Returns true if this event ends later or at the same time as the parameter event. */
  def endsLaterThan(event:CalendarEvent): Boolean=
    this.endTimeInMinutes>=event.endTimeInMinutes

  /** Returns true if this event ends during the parameter event*/
  def endsDuring(event:CalendarEvent): Boolean=
    event.startTimeInMinutes < this.endTimeInMinutes && this.endTimeInMinutes<event.endTimeInMinutes
  /** Returns true if this event starts during the parameter event*/
  def startsDuring(event: CalendarEvent): Boolean=
    this.startTimeInMinutes > event.startTimeInMinutes && this.startTimeInMinutes<event.endTimeInMinutes

  /**Returns true if specified event starts after this event starts and also ends before this event ends ie. this event covers all of the specified event*/
  def covers(event:CalendarEvent): Boolean=
    this.startTimeInMinutes<event.startTimeInMinutes && this.endTimeInMinutes>event.endTimeInMinutes
  /** Returns true if two events "overlap" ie. exist at the same time even for a moment but might not be "covered"*/
  def existsDuring(event: CalendarEvent): Boolean=
    this.endsDuring(event)|| this.startsDuring(event)

  override def toString= s"$startYear/$startMonth/$startDay $startHour:${startTime.substring(11,13)} - $endYear/$endMonth/$endDay $endHour:${endTime.substring(11,13)}"

