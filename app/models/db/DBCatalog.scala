package models.db

/**
 * Catalog database object. A business object has to be mapped to / from this database object 
 * in order to communicate with the database.
 */
case class DBCatalog (idx: Option[Long], id: Option[Long], title: String, author: String,
    publishedYear: Int, category: Int, isDeleted: Boolean)