package models.postgre

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import models.Book

object DBBook {
  val PAGE_ROW_CNT = 5
  
  val bookListMapping = {
    get[Long]("idx") ~
    get[Int]("id") ~
    get[String]("title") ~
    get[String]("author") ~
    get[Int]("publishedYear") map {
      case idx~id~title~author~publishedYear => Book (Some(idx), Some(id), title, author, publishedYear)
    }
  }

  val bookDetailMapping = {
    get[Int]("id") ~
    get[String]("title") ~
    get[String]("author") ~
    get[Int]("publishedYear") map {
      case id~title~author~publishedYear => Book (None, Some(id), title, author, publishedYear)
    }
  }

  	def partial(pageIdx: Int): (List[Book], Int) = DB.withConnection { implicit c =>
  	  val startIdx = (pageIdx-1)*PAGE_ROW_CNT+1
  	  val endIdx = pageIdx*PAGE_ROW_CNT
  	  val list = SQL("select * from (" +
  	  					"select row_number() over(order by id) as idx, * from BOOK" +
  	  				") tempBook where idx<={endIdx} and idx>={startIdx} order by idx")
  	  				.on ('startIdx -> startIdx, 'endIdx -> endIdx)
  	  				.as(bookListMapping *)
  	  val firstRow = SQL("select COUNT(*) c from BOOK").apply.head
  	  val cnt = firstRow[Long]("c")
  	  (list, cnt.toInt)
  	}
  	
  	def all(): List[Book] = DB.withConnection { implicit c =>
	  SQL("select * from (select row_number() over() idx, * from BOOK) " +
	  		"order by idx").as(bookListMapping *)
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
	  			).as(bookDetailMapping *)
	  	list(0)
	  }
	}
	
	def findDuplicates(pBook: Book) = {
	  DB.withConnection { implicit c => 
	    val res = SQL ("select * from BOOK where title={title} and author={author} and publishedYear={publishedYear}")
	    	.on('title -> pBook.title, 'author -> pBook.author, 'publishedYear -> pBook.publishedYear)
	    	.as(bookDetailMapping *)
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