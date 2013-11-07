package models.common

/**
 * Catalog database object. A business object has to be mapped to / from this database object 
 * in order to communicate with the database.
 */
case class CatalogListItem (
    idx: Int, 
    seqNo: Int, 
    categorySeqNo: Int, 
    title: String, 
    author: String,
    publishedYear: Int
)
