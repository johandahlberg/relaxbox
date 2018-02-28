package relaxbot.models

final case class TimeSpent(value: Int) {
  override def toString: String = s"$value"
}
