package common

import play.api.data.validation.Constraint
import java.util.Date
import org.joda.time.DateTime
import play.api.data.validation.Invalid
import play.api.data.validation.Valid
import org.joda.time.Period
import play.api.data.validation.ValidationError

object CommonUtil {
    def validDOB(): Constraint[Date] = 
    Constraint[Date]("constraint.validDate") { o =>
      val currentTime = DateTime.now
      val inputTime = new DateTime(o)
      val threshold = inputTime.plus(Period.years(6))
      if (currentTime.compareTo(threshold)<0 ) {
        Invalid(ValidationError("Minimum age is 6 years old.", 6))
      } else {
        Valid
      }
  }

	def isBlank(str: String) = str==null || str.trim().isEmpty()
}