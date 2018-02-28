package relaxbot.utils

import slack.models.Im
import slack.rtm.SlackRtmClient

object SlackUtils {
  def findChannelForUser(userName: String)(implicit client: SlackRtmClient): Option[Im] = {
    client.state.ims.find { im =>
      client.state.getUserById(im.user).
        exists(user => user.name == userName)
    }
  }
}