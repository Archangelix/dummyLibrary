package models.db

/**
 * User security database object. A business object has to be mapped to / from this database object 
 * in order to communicate with the database.
 */
case class DBUserPassword (
    userID: String, 
    password: String
) extends TDBUserPassword
