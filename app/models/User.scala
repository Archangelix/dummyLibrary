package models

import java.util.Date
import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.Play.current

case class User (seqNo: Option[Long], userID: String, password: String, name: String, address: String, dob: Date)
