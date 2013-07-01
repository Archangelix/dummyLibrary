package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._
import models.h2.DBBook

case class Book (idx: Option[Long], id: Option[Int], title: String, author: String, publishedYear: Int)