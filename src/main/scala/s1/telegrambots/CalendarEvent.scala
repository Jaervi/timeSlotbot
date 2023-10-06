package s1.telegrambots


/*
DTSTART:20180816T080000Z
DTEND:20180816T090000Z
 */

class CalendarEvent (val startTime:String,val endTime:String):


  def duration:Int=
    /*val yearDiff=endYear-startYear
    val monthDiff=endMonth-startMonth
    val dayDiff=endDay-startDay
    val hourDiff=endHour-startHour
    val minuteDiff=endMinute-startMinute*/
    (endYear-startYear)*525600+(endMonth-startMonth)*43200+(endDay-startDay)*1440+(endHour-startHour)*60+(endMinute-startMinute)

  def startYear:Int=startTime.substring(0,4).toInt
  def startMonth:Int=startTime.substring(4,6).toInt
  def startDay:Int=startTime.substring(6,8).toInt
  def startHour:Int=
    if startTime.length>10 then
      startTime.substring(9,11).toInt
    else
      0
  def startMinute:Int=
    if startTime.length>10 then
      startTime.substring(11,13).toInt
    else
      0
  def endYear:Int=endTime.substring(0,4).toInt
  def endMonth:Int=endTime.substring(4,6).toInt
  def endDay:Int=endTime.substring(6,8).toInt
  def endHour:Int=
    if endTime.length>10 then
      endTime.substring(9,11).toInt
    else
      0
  def endMinute:Int=
    if endTime.length>10 then
      endTime.substring(11,13).toInt
    else
      1

  override def toString= startTime + endTime

