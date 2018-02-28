package relaxbot.models

import java.sql.Timestamp

import com.github.nscala_time.time.Imports.DateTime
import relaxbot.dal.DBDriver

import scala.concurrent.ExecutionContext.Implicits.global

case class RelaxEvent(time: DateTime, before: Percentage, after: Percentage, minutesSpent: TimeSpent) {
  override def toString: String = {
    s"${time.toString("yyyy-MM-dd HH:mm")} with ${before} relaxation before " +
      s"event and ${after} relaxation after the event. Time spent was ${minutesSpent}m."
  }
}

object RelaxEventUtils {
  def toTable(relaxEvents: Seq[RelaxEvent]): String = {
    // Sad ad-hoc table formatting. Maybe I'll fix it some day...
    val dateFormatString = "yyyy-MM-dd HH:mm"
    val headerTimeAndSpace = "Time" + Range(0, dateFormatString.length - 4).map(_ => " ").mkString
    val header = s"${headerTimeAndSpace}\tBefore   After\tMinutes spent"
    val previousEvents = relaxEvents.map(e =>
      s"${e.time.toString(dateFormatString)}\t${e.before}\t${e.after}\t${e.minutesSpent}").mkString("\n")
    s"""
       |${header}
       |${previousEvents}
        """.stripMargin
  }
}

trait RelaxEventDBRepresentation {
  this: DBDriver =>
  import driver.api._

  protected val relaxEventsTableQuery = TableQuery[RelaxEvents]

  protected def insertIntoDb(relaxEvent: RelaxEvent): DBIO[RelaxEvent] = {
    (relaxEventsTableQuery += relaxEvent).map(_ => relaxEvent)
  }

  protected class RelaxEvents(tag: Tag) extends Table[RelaxEvent](tag, "RelaxEvents"){

    implicit def dateTime =
      MappedColumnType.base[DateTime, Timestamp](
        dt => new Timestamp(dt.getMillis),
        ts => new DateTime(ts.getTime)
      )

    implicit def percentage =
      MappedColumnType.base[Percentage, Double](
        per => per.value,
        d => Percentage(d)
      )

    implicit def timespent =
      MappedColumnType.base[TimeSpent, Int](
        time => time.value,
        i => TimeSpent(i)
      )

    def time = column[DateTime]("TIME", O.PrimaryKey)
    def before = column[Percentage]("BEFORE")
    def after = column[Percentage]("AFTER")
    def minutesSpent = column[TimeSpent]("MINUTES_SPENT")

    def * = (time, before, after, minutesSpent) <> (RelaxEvent.tupled, RelaxEvent.unapply)
  }
}
