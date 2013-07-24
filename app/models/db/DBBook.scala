package models.db

case class DBBook (id: Option[Long], catalogID: Long, origin: String, remarks: String, isDeleted: Boolean)
