package models.db

case class DBBook (id: Option[Long], catalogID: Long, remarks: String, isDeleted: Boolean)
