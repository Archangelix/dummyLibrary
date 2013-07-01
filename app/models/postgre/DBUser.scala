package models.postgre

import java.util.Date
import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.Play.current
import models.User
import models.exception.UserNotFoundException

object DBUser {
  
	val user = {
	  get[Long]("seqNo") ~
	  get[String]("userID") ~
	  get[String]("password") ~
	  get[String]("name") ~
	  get[String]("address") ~
	  get[Date]("dob") map {
	    case seqNo ~ userID ~ password ~ name ~ address ~ dob =>
	      User(Some(seqNo), userID, password, name, address, dob)
	  }
	}
	
	def findByUserID(pUserID: String): User = {
	  DB.withConnection{ implicit c => 
	    val list = SQL("SELECT * FROM USERS WHERE userid={userID}")
	    	.on('userID -> pUserID).as(user *)
	    if (list==null || list.size==0) {
	      throw UserNotFoundException(pUserID)
	    }
	    list(0)
	  }
	}
	
	def all(): List[User] = {
	  DB.withConnection{ implicit c => 
	    val list = SQL("SELECT * FROM USERS").as(user *)
	    list
	  }
	}
}