package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class Book (id: Option[Int], title: String, author: String, publishedYear: Int)

object Book {
  val book = {
    get[Int]("id") ~
    get[String]("title") ~
    get[String]("author") ~
    get[Int]("publishedYear") map {
      case id~title~author~publishedYear => Book (Some(id), title, author, publishedYear)
    }
  }
  
/*	def unapply(b: Book) = {
	  Some(Option(b.id), b.title, b.author, b.publishedYear)
	}
	
*/	
  	def all(): List[Book] = DB.withConnection { implicit c =>
	  SQL("select * from BOOK order by ID").as(book *)
	}
	
	def create(pTitle: String, pAuthor: String, pPublishedYear: Int) = {
	  DB.withConnection { implicit c => 
	    SQL("insert into BOOK (title, author, publishedYear) values " +
	    		"({title}, {author}, {publishedYear})"
	        ).on('title -> pTitle, 'author -> pAuthor, 'publishedYear -> pPublishedYear
	    ).executeUpdate()
	  }
	}
	
	def findByID(pID: Int) = {
	  DB.withConnection { implicit c =>
	  	val list = SQL("select * from BOOK where id={id}").on('id -> pID
	  			).as(book *)
	  	list(0)
	  }
	}
	def delete(pID: Int) = {
	  DB.withConnection { implicit c =>
	  	SQL("delete from BOOK where id={id}").on('id -> pID
	  			).executeUpdate()
	  }
	}
}