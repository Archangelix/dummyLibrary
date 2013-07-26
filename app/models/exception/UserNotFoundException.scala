package models.exception

/**
 * An exception to be thrown when the requested userID cannot be found
 * in the database.
 */
case class UserNotFoundException(userID: String) extends Exception