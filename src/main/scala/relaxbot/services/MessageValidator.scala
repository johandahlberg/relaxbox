package relaxbot.services

import cats.data._
import cats.implicits._
import com.github.nscala_time.time.Imports.DateTime
import relaxbot.models.{Percentage, RelaxEvent, TimeSpent}

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

  private def validatePercentage(value: Int): ValidationResult[Percentage] = {
    if(value >= 0 && value <= 100) {
      Percentage(value).validNel
    } else {
      InvalidPercentageError.invalidNel
    }
  }

  private def validatedTime(value: Int): ValidationResult[TimeSpent] = {
    if(value > 0) {
      TimeSpent(value).valid
    } else {
      InvalidTimeError.invalidNel
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