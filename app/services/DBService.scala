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
import models.OBUserRole
import models.common.DDBookOriginType
import models.common.DDCountry
import models.common.DDUserRoles
import models.db._
import models.common.Gender

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
	  get[Boolean]("gender") ~
	  get[String]("id_number") ~
	  get[String]("address") ~
	  get[Date]("dob") ~ 
	  get[Long]("user_role_id") ~
	  get[String]("user_role_name") ~
	  get[Long]("nationality") ~
	  get[Boolean]("is_deleted") map {
	    case seqNo~userID~name~gender~idNumber~address~dob~userRoleID~userRoleName~nationality~isDeleted => 
	    	DBUser(None, Some(seqNo), userID, name, gender, idNumber, address, dob, userRoleID, userRoleName, nationality, isDeleted)
	  }
	}
	
	val dbUserListMapping = {
	  get[Long]("rowIdx") ~
	  get[Long]("seqNo") ~
	  get[String]("userID") ~
	  get[String]("name") ~
	  get[Boolean]("gender") ~
	  get[String]("id_number") ~
	  get[String]("address") ~
	  get[Date]("dob") ~ 
	  get[Long]("user_role_id") ~
	  get[String]("user_role_name") ~
	  get[Long]("nationality") ~
	  get[Boolean]("is_deleted") map {
	    case rowIdx~seqNo~userID~name~gender~idNumber~address~dob~userRoleID~userRoleName~nationality~isDeleted =>
	      DBUser(Some(rowIdx), Some(seqNo), userID, name, gender, idNumber, address, dob, userRoleID, userRoleName, nationality, isDeleted)
	  }
	}
	
	val dbOriginTypeMapping = {
	  get[String]("code") ~
	  get[String]("Description") map {
	    case code~description =>
	      DDBookOriginType(code, description)
	  }
	}
	
	val dbCountryMapping = {
	  get[Int]("id") ~
	  get[String]("name") map {
	    case id~name =>
	      DDCountry(id.toString, name)
	  }
	}
	
	val dbUserRoleMapping = {
	  get[Int]("id") ~
	  get[String]("name") map {
	    case id~name =>
	      DDUserRoles(id.toString, name)
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
	    val list = SQL("SELECT USERS.*, USER_ROLE.name USER_ROLE_NAME " +
	    		"FROM USERS, USER_ROLE " +
	    		"WHERE USERS.USER_ROLE_ID = USER_ROLE.ID AND userid={userID}")
	    	.on('userID -> pUserID.toUpperCase()).as(dbUserMapping *)
	    if (list==null || list.size==0) {
	      println("User "+pUserID+" cannot be found!")
	      throw UserNotFoundException(pUserID)
	    }
	    val dbUser = list(0)
	    OBUser(dbUser, OBUserRole(dbUser.userRoleID, dbUser.userRoleName))
	  }
	}
	
	/**
	 * Finding the user based on the ID.
	 * 
	 * @param pUserID The User ID.
	 * @return the User model business object.
	 */
	def findUserBySeqNo(pSeqNo: Long): OBUser = {
	  DB.withConnection{ implicit c => 
	    val list = SQL("SELECT USERS.*, USER_ROLE.name user_role_name " +
	    		"FROM USERS, USER_ROLE " +
	    		"WHERE USERS.USER_ROLE_ID = USER_ROLE.ID AND seqNo={seqNo}")
	    	.on('seqNo -> pSeqNo).as(dbUserMapping *)
	    if (list==null || list.size==0) {
	      println("User with seqNo="+pSeqNo+" cannot be found!")
	      throw UserNotFoundException(pSeqNo.toString)
	    }
	    val dbUser = list(0)
	    OBUser(dbUser, OBUserRole(dbUser.userRoleID, dbUser.userRoleName))
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
	    	.on('userID -> pUserID).apply()
	    if (firstRow.size==0) {
	      println("User "+pUserID+" cannot be found!")
	      throw UserNotFoundException(pUserID)
	    }
	    firstRow.head[String]("password")
	  }
	}
	
  	/**
  	 * Getting the list of all users.
  	 * 
  	 * @return The list of all User business objects.
  	 *
	def all(): List[OBUser] = {
	  DB.withConnection{ implicit c => 
	    val list = SQL("SELECT * FROM USERS").as(dbUserMapping *)
	    list.map(dbUser => OBUser(dbUser))
	  }
	}*/

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
  	 * Insert a new user.
  	 * 
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 */
	def createUser(pUser: OBUser) = {
	  DB.withConnection { implicit c => 
	    SQL("insert into USERS (userid, name, gender, id_number, address, dob, user_role_id, nationality) values " +
	    		"({userid}, {name}, {gender}, {idNumber}, {address}, {dob}, {userRoleID}, {nationality})")
	        .on('userid -> pUser.userID, 'name -> pUser.name, 
	            'gender -> Gender.MALE.equals(pUser.gender), 
	            'idNumber -> pUser.idNumber, 'address -> pUser.address, 
	            'dob -> pUser.dob, 
	            'userRoleID -> pUser.role.id,
	            'nationality -> pUser.nationality)
	        .executeUpdate()
	  }
	}
	
  	/**
  	 * Update an existing user.
  	 * 
  	 * @param pID The ID of the catalog to be updated.
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 */
	def updateUser(pUser: OBUser) = {
	  DB.withConnection { implicit c => 
	    println("seqno = "+pUser.seqNo)
	    println("dob = "+pUser.dob)
	    println("gender = "+pUser.gender)
	    SQL("update USERS set name={name}, address={address}, dob={dob}, "+ 
	    		"user_role_id={user_role_id}, gender={gender}, id_number={id_number}, "+  
	    		"nationality={nationality} "+
	    		"where seqno={seqno}")
	        .on('name -> pUser.name, 
	            'address -> pUser.address,
	            'dob -> pUser.dob, 
	            'user_role_id -> pUser.role.id,
	            'gender -> Gender.MALE.equals(pUser.gender), 
	            'id_number -> pUser.idNumber, 
	            'nationality -> pUser.nationality,
	            'seqno ->pUser.seqNo)
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
	
	def softDeleteUser(pSeqNo: Int) = {
	  DB.withConnection { implicit c =>
	  	SQL("update USERS set is_deleted='Y' where seqNo={seqNo}").on('seqNo -> pSeqNo
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
	
	/**
	 * Fetches the list of users for a particular page.
	 * 
	 * @param pageIDX The page index.
	 * @return The list of users for a particular page.
	 */
	def partialUsers(pageIdx: Int): (List[OBUser], Int) = DB.withConnection { implicit c =>
  	  val startIdx = (pageIdx-1)*PAGE_ROW_CNT+1
  	  val endIdx = pageIdx*PAGE_ROW_CNT
  	  val list = SQL("select * from (" +
  	  					"select row_number() over(order by USERS.SEQNO) as rowIdx, " +
  	  					"	USERS.*, USER_ROLE.ID, USER_ROLE.NAME USER_ROLE_NAME " +
  	  					"from USERS, USER_ROLE " +
  	  					"where USERS.USER_ROLE_ID = USER_ROLE.id" +
  	  				") tempUser " +
  	  				"where rowIdx<={endIdx} and rowIdx>={startIdx} order by rowIdx")
  	  				.on ('startIdx -> startIdx, 'endIdx -> endIdx)
  	  				.as(dbUserListMapping *)
  	  val firstRow = SQL("select COUNT(*) c from USERS").apply.head
  	  val cnt = firstRow[Long]("c")
  	  
  	  (list.map(dbUser => OBUser(dbUser, 
  	      OBUserRole(dbUser.userRoleID, dbUser.userRoleName))
  	  ), cnt.toInt)
  	}
  	
	def getBookOriginTypeMap: Map[String, String] = DB.withConnection { implicit c => 
	  val list = SQL("SELECT * FROM MASTER_BOOK_ORIGIN_TYPE")
	  	.as(dbOriginTypeMapping *)
	  	
	  // Convert from List to Map.
	  Map((list map {s => (s.code, s.desc)}) : _*)
	}
	
	def getCountryMap: Map[String, String] = DB.withConnection { implicit c => 
	  val list = SQL("SELECT * FROM COUNTRY")
	  	.as(dbCountryMapping *)
	  	
	  // Convert from List to Map.
	  val res = Map((list map {s => (s.code, s.desc)}) : _*)
	  println("Size of countries = "+res.size)
	  res
	}

	def getUserRolesMap: Map[String, String] = DB.withConnection { implicit c => 
	  val list = SQL("SELECT * FROM USER_ROLE")
	  	.as(dbUserRoleMapping *)
	  	
	  // Convert from List to Map.
	  val res = Map((list map {s => (s.code, s.desc)}) : _*)
	  res
	}

}