package utils

import play.api.data.validation.Constraint
import java.util.Date
import org.joda.time.DateTime
import play.api.data.validation.Invalid
import play.api.data.validation.Valid
import org.joda.time.Period
import play.api.data.validation.ValidationError
import org.slf4j.LoggerFactory

object CommonUtil {
  val logger = generateLogger(this)
  
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
    
    def mustBeEmpty(): Constraint[String] = 
      Constraint[String]("constraint.invalidInput") { o =>
        println("validating race!")
        if (!isBlank(o)) {
          println("invalid!")
          Invalid(ValidationError("Invalid form input."))
        } else {
          println("valid!")
          Valid
        }
      }
    
    def generateLogger(pObj: Any) = LoggerFactory.getLogger(pObj.getClass().getName())

	def isBlank(str: String): Boolean = str==null || str.trim().isEmpty()
	
	def isBlank(obj: Option[Any]): Boolean = obj==None
	
	def isBlank(list: List[Any]): Boolean = list==null || list.size==0
}