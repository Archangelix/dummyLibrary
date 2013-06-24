package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class Book (idx: Option[Int], id: Option[Int], title: String, author: String, publishedYear: Int)

object Book {
  val PAGE_ROW_CNT = 5
  
  val book = {
    get[Int]("idx") ~
    get[Int]("id") ~
    get[String]("title") ~
    get[String]("author") ~
    get[Int]("publishedYear") map {
      case idx~id~title~author~publishedYear => Book (Some(idx), Some(id), title, author, publishedYear)
    }
  }

  	def partial(pageIdx: Int): (List[Book], Int) = DB.withConnection { implicit c =>
  	  val startIdx = (pageIdx-1)*PAGE_ROW_CNT+1
  	  val endIdx = pageIdx*PAGE_ROW_CNT
  	  val list = SQL("select * from (" +
  	  					"select rownum idx, * from BOOK" +
  	  				") where idx<={endIdx} and idx>={startIdx} order by ID")
  	  				.on ('startIdx -> startIdx, 'endIdx -> endIdx)
  	  				.as(book *)
  	  val firstRow = SQL("select COUNT(*) c from BOOK").apply.head
  	  val cnt = firstRow[Long]("c")
  	  (list, cnt.toInt)
  	}
  	
  	def all(): List[Book] = DB.withConnection { implicit c =>
	  SQL("select rownum idx, * from BOOK order by ID").as(book *)
	}
	
	def create(pTitle: String, pAuthor: String, pPublishedYear: Int) = {
	  DB.withConnection { implicit c => 
	    SQL("insert into BOOK (title, author, publishedYear) values " +
	    		"({title}, {author}, {publishedYear})"
	        ).on('title -> pTitle, 'author -> pAuthor, 'publishedYear -> pPublishedYear
	    ).executeUpdate()
	  }
	}
	
	def update(pID:Int, pTitle: String, pAuthor: String, pPublishedYear: Int) = {
	  DB.withConnection { implicit c => 
	    SQL("update BOOK set title={title}, author={author}, publishedYear={publishedYear} " +
	    		"where ID={id}")
	    	.on('title -> pTitle, 'author -> pAuthor, 'publishedYear -> pPublishedYear, 'id -> pID)
	    	.executeUpdate()
	  }
	}
	
	def findByID(pID: Int) = {
	  DB.withConnection { implicit c =>
	  	val list = SQL("select * from BOOK where id={id}").on('id -> pID
	  			).as(book *)
	  	list(0)
	  }
	}
	
	def findDuplicates(pBook: Book) = {
	  DB.withConnection { implicit c => 
	    val res = SQL ("select * from BOOK where title={title} and author={author} and publishedYear={publishedYear}")
	    	.on('title -> pBook.title, 'author -> pBook.author, 'publishedYear -> pBook.publishedYear)
	    	.as(book *)
	    if (res==null || res.size==0) None else Some(res)
	  }
	}
	
	def delete(pID: Int) = {
	  DB.withConnection { implicit c =>
	  	SQL("delete from BOOK where id={id}").on('id -> pID
	  			).executeUpdate()
	  }
	}
}