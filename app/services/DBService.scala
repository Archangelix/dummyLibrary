package services

import java.util.Date

import anorm._
import anorm.SqlParser._
import models.OBCatalog
import models.OBUser
import models.db._
import models.exception.UserNotFoundException
import play.api.Play.current
import play.api.db._

/**
 * This object serves as a bridge between the Business layer and Database layer.
 * Within this service object the user will process all the database request. 
 * All the results returned from this layer will already be in forms of 
 * Business Object instead of Database object.
 */
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
    get[Int]("publishedYear") ~
    get[Boolean]("is_deleted") map {
      case idx~id~title~author~publishedYear~isDeleted => DBCatalog (Some(idx), Some(id), title, author, publishedYear, isDeleted)
    }
  }

  val dbCatalogDetailMapping = {
    get[Long]("id") ~
    get[String]("title") ~
    get[String]("author") ~
    get[Int]("publishedYear") ~
    get[Boolean]("is_deleted") map {
      case id~title~author~publishedYear~isDeleted => DBCatalog (None, Some(id), title, author, publishedYear, isDeleted)
    }
  }

	val dbUserMapping = {
	  get[Long]("seqNo") ~
	  get[String]("userID") ~
	  get[String]("name") ~
	  get[String]("address") ~
	  get[Date]("dob") ~ 
	  get[Long]("user_role_id") ~
	  get[Boolean]("is_deleted") map {
	    case seqNo~userID~name~address~dob~userRoleID~isDeleted =>
	      DBUser(Some(seqNo), userID, name, address, dob, userRoleID, isDeleted)
	  }
	}
	
  /**
   * END -- All database mappings.
   */
  
  ///////////////////////////////////////////////////////////////////////////////////////  
  
  /**
   * START -- All database APIs.
   */

	/**
	 * Finding the user based on the ID.
	 * 
	 * @param pUserID The User ID.
	 * @return the User model business object.
	 */
	def findByUserID(pUserID: String): OBUser = {
	  DB.withConnection{ implicit c => 
	    val list = SQL("SELECT * FROM USERS WHERE userid={userID}")
	    	.on('userID -> pUserID).as(dbUserMapping *)
	    if (list==null || list.size==0) {
	      println("User "+pUserID+" cannot be found!")
	      throw UserNotFoundException(pUserID)
	    }
	    OBUser(list(0))
	  }
	}
	
	/**
	 * Getting the password for a particular ID. The password information is stored 
	 * in a different table for security reasons.
	 * 
	 * In the future the password should be hashed.
	 * 
	 * @param pUserID The User ID.
	 * @return the password stored in the database.
	 */
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
	
  	/**
  	 * Getting the list of all users.
  	 * 
  	 * @return The list of all User business objects.
  	 */
	def all(): List[OBUser] = {
	  DB.withConnection{ implicit c => 
	    val list = SQL("SELECT * FROM USERS").as(dbUserMapping *)
	    list.map(dbUser => OBUser(dbUser))
	  }
	}

	/**
	 * Insert a new book.
	 * 
	 * @param pCatalogID The catalog ID.
	 * @param pBookID The book ID.
	 * @param pOrigin The book origin.
	 * @param pRemarks The book remarks.
	 */
	def createBook(pCatalogID: Long, pBookID: Long, pOrigin: String, pRemarks: String) = {
	  DB.withConnection { implicit c => 
	    SQL("insert into BOOK (catalog_id, id, remarks, is_deleted, origin) values " +
	    		"({catalogID}, {id}, {remarks}, {isDeleted}, {origin})"
	        ).on('catalogID -> pCatalogID, 'id -> pBookID, 'remarks -> pRemarks, 'isDeleted -> false, 'origin -> pOrigin
	    ).executeUpdate()
	  }
	}
	
	/**
	 * Update a book information.
	 * 
	 * @param pCatalogID The catalog ID.
	 * @param pBookID The book ID.
	 * @param pOrigin The book origin.
	 * @param pRemarks The book remarks.
	 */
	def updateBook(pCatalogID: Long, pBookID: Long, pOrigin: String, pRemarks: String, pIsDeleted: Boolean) = {
	  DB.withConnection { implicit c => 
	    SQL("update BOOK set remarks={author}, is_deleted={isDeleted}, origin={origin} " +
	    		"where catalog_id={title} and ID={id}")
	    	.on('catalogID -> pCatalogID, 'id -> pBookID, 
	    	    'remarks -> pRemarks, 'isDeleted -> pIsDeleted, 'origin -> pOrigin)
	    	.executeUpdate()
	  }
	}
	
	/**
	 * Find all books based on the catalog ID.
	 * 
	 * DUPLICATE -- TO BE REMOVED.
	 * 
	 * @param pCatalogID Catalog ID.
	 */
	def findAllBooksByCatalogID(pCatalogID: Long) = {
	  DB.withConnection { implicit c =>
	  	val list = SQL("select * from BOOK where catalog_id={pCatalogID}").on('pCatalogID -> pCatalogID
	  			).as(dbBookMapping *)
	  	list(0)
	  }
	}
	
	/**
	 * Soft-delete a book based on the book ID.
	 * 
	 * @param pID Book ID.
	 */
	def deleteBook(pID: Long) = {
	  DB.withConnection { implicit c =>
	  	SQL("update BOOK set is_deleted=true where id={id}").on('id -> pID
	  			).executeUpdate()
	  }
	}
	
	/**
	 * Fetches the list of catalogs for a particular page without the books information.
	 * 
	 * @param pageIDX The page index.
	 * @return The list of catalogs for a particular page without the books information.
	 */
	def partialCatalogs(pageIdx: Int): (List[OBCatalog], Int) = partialCatalogs(pageIdx, false)
	
	/**
	 * Fetches the list of catalogs for a particular page with / without the books information.
	 * 
	 * @param pageIDX The page index.
	 * @param pWithBooks The indicator whether to get the books detail as well or not.
	 * @return The list of catalogs for a particular page with / without the books information.
	 */
	def partialCatalogs(pageIdx: Int, pWithBooks: Boolean): (List[OBCatalog], Int) = DB.withConnection { implicit c =>
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
  		    OBCatalog(dbCatalog, books)
  		  }), cnt.toInt)
  	  } else {
  		  (list.map(dbCatalog => OBCatalog(dbCatalog, List[DBBook]())), cnt.toInt)
  	  }
  	}
  	
	/**
	 * Fetches the list of all catalogs without the books information.
	 * 
	 * @param pageIDX The page index.
	 * @return The list of all catalogs without the books information.
	 */
  	def allCatalogs(): List[OBCatalog] = allCatalogs(false)
  	  
	/**
	 * Fetches the list of all catalogs without the books information.
	 * 
	 * @param pageIDX The page index.
	 * @param pWithBooks The indicator whether to get the books detail as well or not.
	 * @return The list of all catalogs.
	 */
  	def allCatalogs(pWithBooks: Boolean): List[OBCatalog] = DB.withConnection { implicit c =>
	  val list = SQL("select * from (select row_number() over() idx, * from CATALOG) " +
	  		"order by idx").as(dbCatalogListMapping *)
	  if (pWithBooks) {
  		  list.map(dbCatalog => {
  		    val books = allBooksByCatalogID(dbCatalog.id.get)
  		    OBCatalog(dbCatalog, books)
  		  })
  	  } else {
  		  list.map(dbCatalog => OBCatalog(dbCatalog, List[DBBook]()))
  	  }
	}
	
  	/**
  	 * Insert a new catalog.
  	 * 
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 */
	def createCatalog(pTitle: String, pAuthor: String, pPublishedYear: Int) = {
	  DB.withConnection { implicit c => 
	    SQL("insert into CATALOG (title, author, publishedYear) values " +
	    		"({title}, {author}, {publishedYear})"
	        ).on('title -> pTitle, 'author -> pAuthor, 'publishedYear -> pPublishedYear
	    ).executeUpdate()
	  }
	}
	
  	/**
  	 * Insert a new catalog.
  	 * 
  	 * @param pID The ID of the catalog to be updated.
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 */
	def updateCatalog(pID: Long, pTitle: String, pAuthor: String, pPublishedYear: Int) = {
	  DB.withConnection { implicit c => 
	    SQL("update CATALOG set title={title}, author={author}, publishedYear={publishedYear} " +
	    		"where ID={id}")
	    	.on('title -> pTitle, 'author -> pAuthor, 'publishedYear -> pPublishedYear, 'id -> pID)
	    	.executeUpdate()
	  }
	}
	
	/**
	 * Fetch a catalog based on the ID. No books detail will be fetched.
	 * 
	 * @param pID The catalog ID to be fetched.
	 * @return The queried catalog.
	 */
	def findCatalogByID(pID: Long): OBCatalog = {
	  findCatalogByID(pID, false)
	}
	
	/**
	 * Fetch a catalog based on the ID. The details will be either with books information
	 * or not depending on the parameter. 
	 * 
	 * @param pID The catalog ID to be fetched.
	 * @param pWithBooks The indicator whether to get the books detail as well or not.
	 * @return The queried catalog.
	 */
	def findCatalogByID(pID: Long, pWithBooks: Boolean): OBCatalog = {
	  DB.withConnection { implicit c =>
	  	val list = SQL("select * from CATALOG where id={id}").on('id -> pID
	  			).as(dbCatalogDetailMapping *)
	  	val dbCatalog = list(0)
	  	if (pWithBooks) {
	  	  println("with books = true")
	  	  val books = allBooksByCatalogID(dbCatalog.id.get)
	  	  println("Number of books = "+books.size)
	  	  OBCatalog(list(0), books)
	  	} else {
	  	  OBCatalog(dbCatalog, List[DBBook]())
	  	}
	  }
	}
	
	/**
	 * Search an existing catalog.
	 * 
	 * @param pCatalog : Catalog object to be compared.
	 * @return <code>None</code> if no duplicates are found. Otherwise it returns the
	 * list of duplicates.
	 */
	def findDuplicates(pCatalog: OBCatalog) = {
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
	
	/**
	 * Find all books based on the catalog ID.
	 * 
	 * @param pCatalogID Catalog ID.
	 * @return the list of books with the same Catalog ID.
	 */
	def allBooksByCatalogID(pCatalogID: Long) = {
	  DB.withConnection { implicit c =>
	  	val list = SQL("select * from BOOK where catalog_id={catalogID}")
	  		.on('catalogID -> pCatalogID).as(dbBookMapping *)
	  	list
	  }
	}
	
	/**
	 * Generate an ID for a new book to be inserted.
	 * 
	 * @param pCatalogID The new book's catalog ID number.
	 * @return the ID of the new book.
	 */
	def generateNewBookID(pCatalogID: Long): Long = {
	  DB.withConnection{ implicit c => 
	    val firstRow = SQL("SELECT coalesce(max(ID), 0) maxID FROM BOOK WHERE CATALOG_ID={catalogID}")
	    	.on('catalogID -> pCatalogID).apply().head
	    println("firstRow = "+firstRow)
	    val dbMaxID = firstRow[Long]("maxID")
    	dbMaxID+1
	  }
	}
	
}