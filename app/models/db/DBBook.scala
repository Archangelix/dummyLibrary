package models.db

/**
 * Book database object. A business object has to be mapped to / from this database object 
 * in order to communicate with the database.
 */
case class DBBook (id: Option[Long], catalogID: Long, origin: String, remarks: String, isDeleted: Boolean)
