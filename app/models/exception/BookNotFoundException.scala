package models.exception

/**
 * An exception to be thrown when the requested bookID cannot be found
 * in the database.
 */
case class BookNotFoundException(bookID: String) extends Exception