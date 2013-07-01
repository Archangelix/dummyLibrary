package models.exception

case class UserNotFoundException(userID: String) extends Exception