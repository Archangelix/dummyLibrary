package services

import java.util.Date

import anorm._
import anorm.SqlParser._
import models.Catalog
import models.User
import models.db._
import models.exception.UserNotFoundException
import play.api.Play.current
import play.api.db._

object DBService {

  /**
   * COMMON CONSTANTS
   */
  val PAGE_ROW_CNT = 5
  
  
  /**
   * START -- All database mappings.
   */
  
  val dbBookMapping = {
    get[Long]("id") ~
    get[Long]("catalog_id") ~
    get[String]("origin") ~
    get[String]("remarks") ~
    get[Boolean]("is_deleted") map {
      case id~catalogID~origin~remarks~isDeleted => DBBook (Some(id), catalogID, origin, remarks, isDeleted)
    }
  }

  val dbCatalogListMapping = {
    get[Long]("idx") ~
    get[Long]("id") ~
    get[String]("title") ~
    get[String]("author") ~
    get[Int]("publishedYear") map {
      case idx~id~title~author~publishedYear => DBCatalog (Some(idx), Some(id), title, author, publishedYear)
    }
  }

  val dbCatalogDetailMapping = {
    get[Long]("id") ~
    get[String]("title") ~
    get[String]("author") ~
    get[Int]("publishedYear") map {
      case id~title~author~publishedYear => DBCatalog (None, Some(id), title, author, publishedYear)
    }
  }

	val dbUserMapping = {
	  get[Long]("seqNo") ~
	  get[String]("userID") ~
	  get[String]("name") ~
	  get[String]("address") ~
	  get[Date]("dob") ~ 
	  get[Long]("user_role_id") map {
	    case seqNo ~ userID ~ name ~ address ~ dob ~ userRoleID =>
	      DBUser(Some(seqNo), userID, name, address, dob, userRoleID)
	  }
	}
	
  /**
   * END -- All database mappings.
   */
  
  ///////////////////////////////////////////////////////////////////////////////////////  
  
  /**
   * START -- All database APIs.
   */

	def findByUserID(pUserID: String): User = {
	  DB.withConnection{ implicit c => 
	    val list = SQL("SELECT * FROM USERS WHERE userid={userID}")
	    	.on('userID -> pUserID).as(dbUserMapping *)
	    if (list==null || list.size==0) {
	      println("User "+pUserID+" cannot be found!")
	      throw UserNotFoundException(pUserID)
	    }
	    User(list(0))
	  }
	}
	
	def getPassword(pUserID: String): String = {
	  DB.withConnection{ implicit c => 
	    val firstRow = SQL("SELECT password FROM USER_SECURITY WHERE userid={userID}")
	    	.on('userID -> pUserID).apply().head
	    if (firstRow==null) {
	      println("User "+pUserID+" cannot be found!")
	      throw UserNotFoundException(pUserID)
	    }
	    firstRow[String]("password")
	  }
	}
	
	def all(): List[User] = {
	  DB.withConnection{ implicit c => 
	    val list = SQL("SELECT * FROM USERS").as(dbUserMapping *)
	    list.map(dbUser => User(dbUser))
	  }
	}

	def createBook(pCatalogID: Long, pBookID: Long, pOrigin: String, pRemarks: String) = {
	  DB.withConnection { implicit c => 
	    SQL("insert into BOOK (catalog_id, id, remarks, is_deleted, origin) values " +
	    		"({catalogID}, {id}, {remarks}, {isDeleted}, {origin})"
	        ).on('catalogID -> pCatalogID, 'id -> pBookID, 'remarks -> pRemarks, 'isDeleted -> false, 'origin -> pOrigin
	    ).executeUpdate()
	  }
	}
	
	def updateBook(pCatalogID: Long, pBookID: Long, pOrigin: String, pRemarks: String, pIsDeleted: Boolean) = {
	  DB.withConnection { implicit c => 
	    SQL("update BOOK set remarks={author}, is_deleted={isDeleted}, origin={origin} " +
	    		"where catalog_id={title} and ID={id}")
	    	.on('catalogID -> pCatalogID, 'id -> pBookID, 
	    	    'remarks -> pRemarks, 'isDeleted -> pIsDeleted, 'origin -> pOrigin)
	    	.executeUpdate()
	  }
	}
	
	def findByAllBooksByCatalogID(pCatalogID: Long) = {
	  DB.withConnection { implicit c =>
	  	val list = SQL("select * from BOOK where catalog_id={pCatalogID}").on('pCatalogID -> pCatalogID
	  			).as(dbBookMapping *)
	  	list(0)
	  }
	}
	
	def deleteBook(pID: Long) = {
	  DB.withConnection { implicit c =>
	  	SQL("update BOOK set is_deleted=true where id={id}").on('id -> pID
	  			).executeUpdate()
	  }
	}
	
	def partialCatalogs(pageIdx: Int): (List[Catalog], Int) = partialCatalogs(pageIdx, false)
	
	def partialCatalogs(pageIdx: Int, pWithBooks: Boolean): (List[Catalog], Int) = DB.withConnection { implicit c =>
  	  val startIdx = (pageIdx-1)*PAGE_ROW_CNT+1
  	  val endIdx = pageIdx*PAGE_ROW_CNT
  	  val list = SQL("select * from (" +
  	  					"select row_number() over(order by id) as idx, * from CATALOG" +
  	  				") tempBook where idx<={endIdx} and idx>={startIdx} order by idx")
  	  				.on ('startIdx -> startIdx, 'endIdx -> endIdx)
  	  				.as(dbCatalogListMapping *)
  	  val firstRow = SQL("select COUNT(*) c from CATALOG").apply.head
  	  val cnt = firstRow[Long]("c")
  	  if (pWithBooks) {
  		  (list.map(dbCatalog => {
  		    val books = allBooksByCatalogID(dbCatalog.id.get)
  		    Catalog(dbCatalog, books)
  		  }), cnt.toInt)
  	  } else {
  		  (list.map(dbCatalog => Catalog(dbCatalog, List[DBBook]())), cnt.toInt)
  	  }
  	}
  	
  	def allCatalogs(): List[Catalog] = allCatalogs(false)
  	  
  	def allCatalogs(pWithBooks: Boolean): List[Catalog] = DB.withConnection { implicit c =>
	  val list = SQL("select * from (select row_number() over() idx, * from CATALOG) " +
	  		"order by idx").as(dbCatalogListMapping *)
	  if (pWithBooks) {
  		  list.map(dbCatalog => {
  		    val books = allBooksByCatalogID(dbCatalog.id.get)
  		    Catalog(dbCatalog, books)
  		  })
  	  } else {
  		  list.map(dbCatalog => Catalog(dbCatalog, List[DBBook]()))
  	  }
	}
	
	def createCatalog(pTitle: String, pAuthor: String, pPublishedYear: Int) = {
	  DB.withConnection { implicit c => 
	    SQL("insert into CATALOG (title, author, publishedYear) values " +
	    		"({title}, {author}, {publishedYear})"
	        ).on('title -> pTitle, 'author -> pAuthor, 'publishedYear -> pPublishedYear
	    ).executeUpdate()
	  }
	}
	
	def updateCatalog(pID: Long, pTitle: String, pAuthor: String, pPublishedYear: Int) = {
	  DB.withConnection { implicit c => 
	    SQL("update CATALOG set title={title}, author={author}, publishedYear={publishedYear} " +
	    		"where ID={id}")
	    	.on('title -> pTitle, 'author -> pAuthor, 'publishedYear -> pPublishedYear, 'id -> pID)
	    	.executeUpdate()
	  }
	}
	
	def findCatalogByID(pID: Long): Catalog = {
	  findCatalogByID(pID, false)
	}
	
	def findCatalogByID(pID: Long, pWithBooks: Boolean): Catalog = {
	  DB.withConnection { implicit c =>
	  	val list = SQL("select * from CATALOG where id={id}").on('id -> pID
	  			).as(dbCatalogDetailMapping *)
	  	val dbCatalog = list(0)
	  	if (pWithBooks) {
	  	  println("with books = true")
	  	  val books = allBooksByCatalogID(dbCatalog.id.get)
	  	  println("Number of books = "+books.size)
	  	  Catalog(list(0), books)
	  	} else {
	  	  Catalog(dbCatalog, List[DBBook]())
	  	}
	  }
	}
	
	def findDuplicates(pCatalog: Catalog) = {
	  DB.withConnection { implicit c => 
	    val res = SQL ("select * from CATALOG where title={title} and author={author} and publishedYear={publishedYear}")
	    	.on('title -> pCatalog.title, 'author -> pCatalog.author, 'publishedYear -> pCatalog.publishedYear)
	    	.as(dbCatalogDetailMapping *)
	    if (res==null || res.size==0) None else Some(res)
	  }
	}
	
	def deleteCatalog(pID: Int) = {
	  DB.withConnection { implicit c =>
	  	SQL("delete from CATALOG where id={id}").on('id -> pID
	  			).executeUpdate()
	  }
	}
	
	def allBooksByCatalogID(pCatalogID: Long) = {
	  DB.withConnection { implicit c =>
	  	val list = SQL("select * from BOOK where catalog_id={catalogID}")
	  		.on('catalogID -> pCatalogID).as(dbBookMapping *)
	  	list
	  }
	}
	
	def generateNewBookID(pCatalogID: Long): Long = {
	  DB.withConnection{ implicit c => 
	    val firstRow = SQL("SELECT max(ID) maxID FROM BOOK WHERE CATALOG_ID={catalogID}")
	    	.on('catalogID -> pCatalogID).apply().head
	    if (firstRow==null) {
	      1
	    }
	    firstRow[Long]("maxID")+1
	  }
	}
	
}