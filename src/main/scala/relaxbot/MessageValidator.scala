package relaxbot

import cats.data._
import cats.implicits._
import com.github.nscala_time.time.Imports.DateTime

object MessageValidator {
  sealed trait ValidationError {
    val errorMessage: String
  }
  case object InvalidPercentageError extends ValidationError {
    val errorMessage: String = "Percentages need to be between 0 and 100"
  }
  case object InvalidTimeError extends ValidationError {
    val errorMessage: String = "Times must be greater than 0."
  }
  case object NotEnoughFieldsInMessage extends ValidationError {
    val errorMessage: String = "Not enough fields provided in message"
  }

  type ValidationResult[A] = ValidatedNel[ValidationError, A]

  final case class Percentage(value: Double) {
    override def toString: String = {
      s"${value}%"
    }
  }
  private def validatePercentage(value: Int): ValidationResult[Percentage] = {
    if(value >= 0 && value <= 100) {
      Percentage(value).validNel
    } else {
      InvalidPercentageError.invalidNel
    }
  }

  final case class TimeSpent(value: Int) {
    override def toString: String = s"$value"
  }
  private def validatedTime(value: Int): ValidationResult[TimeSpent] = {
    if(value > 0) {
      TimeSpent(value).valid
    } else {
      InvalidTimeError.invalidNel
    }
  }

  final case class RelaxEvent(time: DateTime, before: Percentage, after: Percentage, minutesSpent: TimeSpent) {
    override def toString: String = {
      s"${time.toString("yyyy-MM-dd HH:mm")} with ${before} relaxation before " +
        s"event and ${after} relaxation after the event. Time spent was ${minutesSpent}m."
    }
  }

  def validateMessage(message: String): ValidationResult[RelaxEvent] = {
    message.split(" ").toList match {
      case _ :: before :: after :: timeSpent :: _ => {
        (DateTime.now().valid,
          validatePercentage(before.toInt),
          validatePercentage(after.toInt),
          validatedTime(timeSpent.toInt)).mapN(RelaxEvent)
      }
      case _ => NotEnoughFieldsInMessage.invalidNel
    }
  }
}