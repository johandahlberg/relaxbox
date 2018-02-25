package relaxbot

import akka.actor.ActorSystem
import cats.data.Validated.{Invalid, Valid}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.scalalogging.StrictLogging
import slack.rtm.SlackRtmClient


object RelaxBot extends StrictLogging {

  def main(args: Array[String]): Unit = {

    val token = sys.env("SLACK_TOKEN")
    val myUser = sys.env("SLACK_USER")

    implicit val system = ActorSystem("relaxbot")
    implicit val ec = system.dispatcher

    val client = SlackRtmClient(token)
    val selfId = client.state.self.id

    val reminder = system.actorOf(Reminder.props(client))
    QuartzSchedulerExtension(system).
      createSchedule(name = "EveryDayAt8And20", cronExpression = "0 0 8,20 ? * * *")
    QuartzSchedulerExtension(system).
      schedule("EveryDayAt8And20", reminder, ReminderMessage(myUser))

    client.onMessage { message =>
      logger.debug(s"Got message $message")
      logger.debug(s"Channel: ${message.channel}")
      if(message.text.startsWith("relax")) {
        val relaxEvent = MessageValidator.validateMessage(message.text)
        relaxEvent match {
          case Valid(v) =>
            client.sendMessage(message.channel, s"Registered relax event: ${v}")
          case Invalid(e) =>
            client.sendMessage(message.channel, s"Could not parse relaxation event. Error: $e")
        }
      }
    }
  }
}
