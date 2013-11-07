package models.form

import java.util.Date

/**
 * This case class is used to represent the borrowing admin page. 
 */
case class FormBorrow(
  val adminUsername: String,
  val borrowerID: Option[String],
  val borrowerName: Option[String],
  val borrowerAddress: Option[String],
  val borrowDate: Option[Date],
  val returnDate: Option[Date],
  val newBookID: Option[String],
  val books: Option[List[FormBook]]
)