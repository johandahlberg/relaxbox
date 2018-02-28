package relaxbot.models

final case class Percentage(value: Double) {
  override def toString: String = {
    s"${value}%"
  }
}
