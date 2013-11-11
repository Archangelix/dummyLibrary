package models.exception

/**
 * An exception to be thrown when the requested bookID is not available
 * for borrowing transaction.
 */
case class BookNotAvailableException(bookID: String) extends Exception