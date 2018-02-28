package relaxbot

import akka.actor.ActorSystem
import cats.data.Validated.{Invalid, Valid}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.scalalogging.StrictLogging
import org.joda.time.DateTime
import relaxbot.dal.DatabaseAccessLayer
import relaxbot.models.{Percentage, RelaxEvent, RelaxEventUtils, ReminderMessage, TimeSpent}
import relaxbot.services.{MessageValidator, Reminder}
import slack.rtm.SlackRtmClient

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import slick.jdbc.SQLiteProfile
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


object RelaxBot extends StrictLogging {

  def main(args: Array[String]): Unit = {

    val token = sys.env("SLACK_TOKEN")
    val myUser = sys.env("SLACK_USER")
    val dbUrl = sys.env("DB_URL")

    object DB extends DatabaseAccessLayer {
      logger.info(s"$dbUrl")
      val db = Database.forURL(dbUrl, driver="org.sqlite.JDBC")
      val driver = SQLiteProfile

      sys.ShutdownHookThread {
        logger.info("Shutting down db connection")
        DB.closeDB()
      }
    }

    Await.result(DB.createSchemaIfNotExists, Duration.Inf)

    implicit val system = ActorSystem("relaxbot")
    implicit val ec = system.dispatcher

    val client = SlackRtmClient(token)

    val reminder = system.actorOf(Reminder.props(client))
    QuartzSchedulerExtension(system).
      createSchedule(name = "EveryDayAt8And20", cronExpression = "0 0 8,20 ? * * *")
    QuartzSchedulerExtension(system).
      schedule("EveryDayAt8And20", reminder, ReminderMessage(myUser))

    client.onMessage { message =>
      message.text match {
        case s: String if s.startsWith("relax") => {
          logger.info("Got relax message")
          val relaxEvent = MessageValidator.validateMessage(message.text)
          relaxEvent match {
            case Valid(v) =>
              DB.insert(v).onComplete {
                case Success(v) =>
                  client.sendMessage(message.channel, s"Registered relax event: ${v}")
                case Failure(e) =>
                  client.sendMessage(message.channel, s"Failed to update database: ${e}")
              }

            case Invalid(e) =>
              client.sendMessage(message.channel, s"Could not parse relaxation event. Error: $e")
          }
        }
        case s: String if s.startsWith("export") => {
          logger.info("Got export message")

          DB.relaxEvents.onComplete {
            case Success(relaxEvents) =>
              client.sendMessage(message.channel,
                s"""```
                   |${RelaxEventUtils.toTable(relaxEvents)}
                   |```
                   |""".stripMargin)
            case Failure(error) =>
              client.sendMessage(message.channel, s"Error in getting data to export: ${error}")
          }

        }
      }
    }
  }
}
