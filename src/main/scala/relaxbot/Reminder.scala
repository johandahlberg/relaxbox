package relaxbot

import akka.actor.{Actor, ActorLogging, Props}
import slack.rtm.SlackRtmClient

final case class ReminderMessage(userName: String)

class Reminder(slackClient: SlackRtmClient) extends Actor with ActorLogging {
  override def receive: Receive = {
    case ReminderMessage(userName) =>
      SlackUtils.findChannelForUser(userName)(slackClient) match {
        case Some(channel) => slackClient.sendMessage(channel.id, "Time to relax...")
        case None => log.error("Could not find a channel to send to!")
      }
  }
}
object Reminder {
  def props(slackClient: SlackRtmClient): Props = {
    Props(new Reminder(slackClient))
  }
}
