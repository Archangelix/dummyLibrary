package services

import java.security.SecureRandom
import java.sql.Connection
import java.util.Date
import anorm._
import anorm.SqlParser._
import models.common.CatalogListItem
import models.common.Category
import models.common.DDBookOrigin
import models.common.DDCountry
import models.common.DDUserRoles
import models.common.Gender
import models.common.UserListItem
import models.common.UserRole
import models.db._
import models.exception.BookNotFoundException
import models.exception.CatalogNotFoundException
import models.exception.CategoryNotFoundException
import models.exception.UserNotFoundException
import play.api.Play.current
import play.api.db._
import utils.SecurityUtil
import java.sql.SQLException
import models.common.STATUS_BORROW_HD_DFT

/**
 * This object serves as a Database layer.
 * Within this service object the user will process all the database request. 
 * All the results returned from this layer will be in forms of Database objects.
 */
object PSQLService extends TDBService {

  /**
   * START -- All database APIs.
   */

	/**
	 * Finding the user based on the ID.
	 * 
	 * @param pUserID The User ID.
	 * @return the User model business object.
	 */
	def findByUserID(pUserID: String)(implicit pIncludeDeleted: Boolean = false): TDBUser = {
	  logger.debug("findByUserID")
	  DB.withConnection{ implicit c => 
	    val list = SQL("SELECT * FROM USERS WHERE upper(userid)=upper({userID})")
	    	.on('userID -> pUserID.toUpperCase()).as(dbUserMapping *)
	    if (list==null || list.size==0) {
	      logger.debug("User "+pUserID+" cannot be found!")
	      throw UserNotFoundException(pUserID)
	    }
	    val dbUser = list(0)
	    dbUser
	  }
	}
	
	/**
	 * Finding the user based on the ID.
	 * 
	 * @param pUserID The User ID.
	 * @return the User model business object.
	 */
	def findUserByIDNumber(pIDNumber: String)(implicit pIncludeDeleted: Boolean = false): TDBUser = {
	  DB.withConnection{ implicit c => 
	    val list = SQL("SELECT * FROM USERS WHERE upper(id_number)={idNumber}")
	    	.on('idNumber -> pIDNumber.toUpperCase()).as(dbUserMapping *)
	    if (list==null || list.size==0) {
	      logger.debug("ID Number "+pIDNumber+" cannot be found!")
	      throw UserNotFoundException(pIDNumber)
	    }
	    val dbUser = list(0)
	    dbUser
	  }
	}
	
	/**
	 * Finding the user based on the ID.
	 * 
	 * @param pUserID The User ID.
	 * @return the User model business object.
	 */
	def findUserBySeqNo(pSeqNo: Int)(implicit pIncludeDeleted: Boolean = false): TDBUser = {
	  DB.withConnection{ implicit c => 
	    val list = SQL("SELECT * FROM USERS WHERE seqNo={seqNo}")
	    	.on('seqNo -> pSeqNo).as(dbUserMapping *)
	    if (list==null || list.size==0) {
	      logger.debug("User with seqNo="+pSeqNo+" cannot be found!")
	      throw UserNotFoundException(pSeqNo.toString)
	    }
	    val dbUser = list(0)
	    dbUser
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
	    val firstRow = SQL("SELECT password FROM USER_SECURITY WHERE upper(userid)=upper({userID})")
	    	.on('userID -> pUserID).apply()
	    if (firstRow.size==0) {
	      logger.debug("User "+pUserID+" cannot be found!")
	      throw UserNotFoundException(pUserID)
	    }
	    firstRow.head[String]("password")
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
	def createBook(pBook: TDBBook)(implicit pOfficerUserID: String) = {
	  logger.debug("createBook")
	  DB.withConnection { implicit c => 
	    SQL("insert into BOOK (catalog_seqno, seqno, remarks, " +
	    		"status, status_usercode, status_timestamp, " +
	    		"is_deleted, origin, " +
	    		"create_usercode, create_timestamp, audit_usercode, audit_timestamp, audit_reason) " +
	    	"values ({catalogSeqNo}, {seqno}, {remarks}, " +
	    		"{status}, {statusUsercode}, now(), " +
	    		"{isDeleted}, {origin}, " +
	    		"{createUsercode}, now(), {auditUsercode}, now(), '')")
	    	.on('catalogSeqNo -> pBook.catalogSeqNo, 
	            'seqno -> pBook.seqNo.get, 
	            'remarks -> pBook.remarks,
	            'status -> pBook.status,
	            'statusUsercode -> pBook.statusUsercode,
	            'isDeleted -> false, 
	            'origin -> pBook.origin,
	            'createUsercode -> pOfficerUserID, 
	            'auditUsercode -> pOfficerUserID
	            )
	        .executeUpdate()
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
	def updateBook(pBook: TDBBook)(implicit pOfficerUserID: String) = {
	  DB.withConnection { implicit c => 
	    SQL("update BOOK " +
	    		"set remarks={remarks}, " +
	    		"	origin={origin}, " +
	    		"	status={status}, " +
	    		"	status_usercode={statusUsercode}, " +
	    		"	status_timestamp={statusTimestamp}, " +
	    		"	is_deleted={isDeleted}, " +
	    		"	audit_usercode = {auditUsercode}, " +
	    		"	audit_timestamp = now() " +
	    		"where catalog_seqNo={catalogSeqNo} and " +
	    		"	seqno={seqNo}")
	    	.on('catalogSeqNo -> pBook.catalogSeqNo, 
	    	    'seqNo -> pBook.seqNo, 
	    	    'remarks -> pBook.remarks, 
	            'status -> pBook.status,
	            'statusUsercode -> pBook.statusUsercode,
	            'statusTimestamp -> pBook.statusTimestamp,
	            'isDeleted -> pBook.isDeleted, 
	    	    'origin -> pBook.origin,
	    	    'auditUsercode -> pOfficerUserID)
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
	def findAllBooksByCatalogID(pCatalogSeqNo: Int)
	(implicit pIncludeDeleted: Boolean = false): List[TDBBook] = {
	  logger.debug("findAllBooksByCatalogID")
	  DB.withConnection { implicit c =>
	    val sql = 
	      if (pIncludeDeleted) {
	        "select * from BOOK where catalog_seqno={catalogSeqNo}"
	      } else {
	        "select * from BOOK where catalog_seqno={catalogSeqNo} and not is_deleted"
	      }
	  	val list = SQL(sql)
	  	  	.on('catalogSeqNo -> pCatalogSeqNo)
	  	  	.as(dbBookMapping *)
	  	list
	  }
	}
	
	/**
	 * Find all books based on the catalog ID.
	 * 
	 * DUPLICATE -- TO BE REMOVED.
	 * 
	 * @param pCatalogID Catalog ID.
	 */
	def findBook(pCatalogSeqNo: Int, pBookSeqNo: Int)
		(implicit pIncludeDeleted: Boolean = false): TDBBook = {
	  DB.withConnection { implicit c =>
	    val sql =
	      if (pIncludeDeleted) {
	        """
	        select * from BOOK 
	        where catalog_seqno={catalogSeqNo} and seqno={bookSeqNo}
	        """
	      } else {
	        """
	        select * from BOOK 
	        where catalog_seqno={catalogSeqNo} and seqno={bookSeqNo} and not is_deleted
	        """
	      }
	  	val list = SQL(sql)
	  		.on('catalogSeqNo -> pCatalogSeqNo,
	  		    'bookSeqNo -> pBookSeqNo)
	  		.as(dbBookMapping *)
	  	if (list.size==0) {
	  	  throw BookNotFoundException(pCatalogSeqNo.toString+"."+pBookSeqNo.toString)
	  	}
	  	list(0)
	  }
	}
	
	/**
	 * Soft-delete a book based on the book ID.
	 * 
	 * @param pID Book ID.
	 */
	def deleteBook(pCatalogSeqNo: Int, pBookSeqNo: Int)(implicit pOfficerUserID: String ) = {
	  DB.withConnection { implicit c =>
	  	SQL("update BOOK set is_deleted=true, " +
	  			"audit_usercode={auditUsercode}, " +
	  			"audit_timestamp=now() " +
	  		"where catalog_seqno={catalogSeqNo} and seqNo={bookSeqNo}")
	  	.on('auditUsercode -> pOfficerUserID, 
	  	    'catalogSeqNo -> pCatalogSeqNo,
	  	    'bookSeqNo -> pBookSeqNo
	  	    )
	  	.executeUpdate()
	  }
	}
	
	/**
	 * Fetches the list of catalogs for a particular page with / without the books information.
	 * 
	 * @param startIdx The starting row index.
	 * @param endIdx The ending row index.
	 * @param pWithBooks The indicator whether to get the books detail as well or not.
	 * @return The list of catalogs for a particular page with / without the books information.
	 */
	def partialCatalogs(startIdx: Int, endIdx: Int, pWithBooks: Boolean): (List[CatalogListItem], Int) = 
	  	DB.withConnection { implicit c =>
  	  val list = SQL("select * from (" +
  	  					"select row_number() over(order by seqno) as idx, * from CATALOG" +
  	  				") tempBook where idx<={endIdx} and idx>={startIdx} order by idx")
  	  				.on ('startIdx -> startIdx, 'endIdx -> endIdx)
  	  				.as(dbCatalogListMapping *)
  	  val firstRow = SQL("select COUNT(*) c from CATALOG").apply.head
  	  val cnt = firstRow[Long]("c")
  	  (list, cnt.toInt)
  	}
  	
  	/**
  	 * Insert a new catalog.
  	 * 
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 * @param pCategory The catalog category.
  	 */
	def insertCatalog(pCatalog: TDBCatalog)(implicit pOfficerUserID: String) = {
	  DB.withConnection { implicit c => 
	    SQL("insert into CATALOG " +
	    		"(title, author, published_year, category_seqno, arrival_date, " +
	    		"create_usercode, create_timestamp, audit_usercode, audit_timestamp, audit_reason) " +
	    	"values ({title}, {author}, {publishedYear}, {category}, now(), " +
	    		"{createUsercode}, now(), {auditUsercode}, now(), '')"
	        ).on('title -> pCatalog.title, 
	            'author -> pCatalog.author, 
	            'publishedYear -> pCatalog.publishedYear,
	            'category -> pCatalog.categorySeqNo,
	            'createUsercode -> pOfficerUserID, 
	            'auditUsercode -> pOfficerUserID
	    ).executeInsert()
	  } match {
	    case Some(long) => long.toInt
	    case _ => throw new SQLException("Error in inserting a new catalog")
	  }
	}
	
  	/**
  	 * Update an existing catalog.
  	 * 
  	 * @param pID The ID of the catalog to be updated.
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 * @param pCategory The catalog category.
  	 */
	def updateCatalog(pCatalog: TDBCatalog)(implicit pOfficerUserID: String) = {
	  DB.withConnection { implicit c => 
	    SQL("update CATALOG " +
	    		"set title = {title}, " +
	    		"	author = {author}, " +
	    		"	published_year = {publishedYear}, " +
	    		"	category_seqno = {category}, " +
	    		"	audit_usercode = {auditUsercode}, " +
	    		"	audit_timestamp = now() " +
	    		"where SEQNO={seqno}")
	    	.on('title -> pCatalog.title, 
	    	    'author -> pCatalog.author, 
	    	    'publishedYear -> pCatalog.publishedYear,
	            'category -> pCatalog.categorySeqNo, 
	            'auditUsercode -> pOfficerUserID, 
	            'seqno -> pCatalog.seqNo)
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
	def createUser(pUser: TDBUser)(implicit pOfficerUserID: String, c: Connection) : Int = {
	  SQL("insert into USERS (userid, name, gender, id_number, address, dob, user_role_seqno, nationality, " +
	    		"create_usercode, create_timestamp, audit_usercode, audit_timestamp, audit_reason) values " +
	    		"({userid}, {name}, {gender}, {idNumber}, {address}, {dob}, {userRoleSeqNo}, {nationality}, " +
	    		"{createUsercode}, now(), {auditUsercode}, now(), '')")
	        .on('userid -> pUser.userID, 
	            'name -> pUser.name, 
	            'gender -> Gender.MALE.equals(pUser.gender), 
	            'idNumber -> pUser.idNumber, 'address -> pUser.address, 
	            'dob -> pUser.dob, 
	            'userRoleSeqNo -> pUser.userRoleSeqNo,
	            'nationality -> pUser.nationality,
	            'createUsercode -> pOfficerUserID, 
	            'auditUsercode -> pOfficerUserID)
	        .executeUpdate()
	}
	
	def createUserAndPassword(pUser: TDBUser, pPassword: String)(implicit pOfficerUserID: String) = {
	  DB.withTransaction { implicit c => 
	    createUser(pUser)
        createPassword(pUser.userID, pPassword)
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
	def updateUser(pUser: TDBUser)(implicit pOfficerUserID: String) = {
	  DB.withConnection { implicit c => 
	    logger.debug("seqno = "+pUser.seqNo)
	    logger.debug("dob = "+pUser.dob)
	    logger.debug("gender = "+pUser.gender)
	    SQL("update USERS set name={name}, " +
	    		"	address={address}, " +
	    		"	dob={dob}, "+ 
	    		"	user_role_seqno={user_role_seqno}, " +
	    		"	gender={gender}, " +
	    		"	id_number={id_number}, "+  
	    		"	nationality={nationality}, " +
	    		"	audit_usercode = {auditUsercode}, " +
	    		"	audit_timestamp = now() " +
	    		"where seqno={seqno}")
	        .on('name -> pUser.name, 
	            'address -> pUser.address,
	            'dob -> pUser.dob, 
	            'user_role_seqno -> pUser.userRoleSeqNo,
	            'gender -> Gender.MALE.equals(pUser.gender), 
	            'id_number -> pUser.idNumber, 
	            'nationality -> pUser.nationality,
	            'auditUsercode -> pOfficerUserID,
	            'seqno ->pUser.seqNo)
	    	.executeUpdate()
	  }
	}
	
	def createPassword(pUserID: String, pPassword: String)(implicit pOfficerUserID: String, c: Connection) = {
	  val sr = new SecureRandom()
	  var bytes = new Array[Byte](32)
	  sr.nextBytes(bytes)
	  val encryptedPwd = SecurityUtil.hex_digest(bytes.toString()+pPassword)
	  val securedPassword = bytes.toString() + "|" + encryptedPwd
      SQL("insert into USER_SECURITY (userid, password, " +
      		"create_usercode, create_timestamp, audit_usercode, audit_timestamp, audit_reason) values " +
    		"({userid}, {password}, " +
    		"{createUsercode}, now(), {auditUsercode}, now(), '')")
        .on('userid -> pUserID,
            'password -> securedPassword,
	        'createUsercode -> pOfficerUserID, 
	        'auditUsercode -> pOfficerUserID)
        .executeUpdate()
	}
	
	def updatePassword(pUserID: String, pPassword: String)(implicit pOfficerUserID: String) = {
	  val sr = new SecureRandom()
	  var bytes = new Array[Byte](32)
	  sr.nextBytes(bytes)
	  val encryptedPwd = SecurityUtil.hex_digest(bytes.toString()+pPassword)
	  val securedPassword = bytes.toString() + "|" + encryptedPwd
	  DB.withConnection { implicit c =>
	    SQL("update USER_SECURITY " +
	    		"set password = {password}, " +
	    		"	audit_usercode = {auditUsercode}, " +
	    		"	audit_timestamp = now() " +
	    		"where userid={userid} ")
	        .on('userid -> pUserID,
	            'password -> securedPassword,
	            'auditUsercode -> pOfficerUserID)
	        .executeUpdate()
	  }
	}
	
	/**
	 * Fetch a catalog based on the ID. The details may or may not include books information
	 * depending on the parameter. 
	 * 
	 * @param pID The catalog ID to be fetched.
	 * @param pWithBooks The indicator whether to get the books detail as well or not.
	 * @return The queried catalog.
	 */
	def findCatalogByID(pSeqNo: Int)(implicit pIncludeDeleted: Boolean = false): TDBCatalog = {
	  DB.withConnection { implicit c =>
	  	val list = 
	  	  SQL("select * from CATALOG where SEQNO={seqNo}")
	  	  	.on('seqNo -> pSeqNo)
	  	  	.as(dbCatalogDetailMapping *)
	  	if (list.size==0) {
	  	  throw CatalogNotFoundException(pSeqNo)
	  	}
	  	val dbCatalog = list(0)
	  	dbCatalog
	  }
	}
	
	/**
	 * Fetch a book based on the ID. 
	 * 
	 * @param pID The book ID to be fetched.
	 * @return The queried book.
	 */
	def getBorrowTransaction(pTransactionSeqNo: Int): TDBTxBorrowHD = {
	  logger.debug("getBorrowTransaction")
	  DB.withConnection { implicit c =>
	    val sql = """
			select * from tx_borrow_hd where seqno = {seqno} 
	      """
	  	val list = SQL(sql)
	  		.on('seqno -> pTransactionSeqNo)
	  		.as(dbTxBorrowHDMapping *)
	  	val res = list(0)
	  	res
	  }
	}
	
	/**
	 * Fetch a book based on the ID. 
	 * 
	 * @param pID The book ID to be fetched.
	 * @return The queried book.
	 */
	def getTransactionDetailsByID(pTransactionID: Int): List[TDBTxBorrowDT] = {
	  logger.debug("getTransactionDetailByIdD")
	  DB.withConnection { implicit c =>
	    val sql = "select * from tx_borrow_dt where hd_seqno = {seqno}"
	  	val list = SQL(sql).on('seqno -> pTransactionID).as(dbTxBorrowDTMapping *)
	  	list
	  }
	}
	
	def findPendingTxByBookID(pCatalogSeqNo: Int, pBookSeqNo: Int): TDBTxBorrowDT = {
	  logger.debug("findPendingTxByBookID")
	  DB.withConnection { implicit c =>
	    val sql = """
	      select * from tx_borrow_dt 
	      where catalog_seqno = {catalogSeqNo} and book_seqno = {bookSeqNo}
	      	and status='PEN'
	    		"""
	  	val list = SQL(sql)
	  		.on('catalogSeqNo -> pCatalogSeqNo,
	  		    'bookSeqNo -> pBookSeqNo)
	  		.as(dbTxBorrowDTMapping *)
	  	val res = list(0)
	    res
	  }
	}
	
	/**
	 * Search an existing catalog.
	 * 
	 * @param pCatalog : Catalog object to be compared.
	 * @return <code>None</code> if no duplicates are found. Otherwise it returns the
	 * list of duplicates.
	 */
	def findDuplicates(pCatalog: TDBCatalog): Option[List[TDBCatalog]] = {
	  DB.withConnection { implicit c => 
	    val res = SQL ("select * from CATALOG where title={title} and author={author} and published_year={publishedYear}")
	    	.on('title -> pCatalog.title, 
	    	    'author -> pCatalog.author, 
	    	    'publishedYear -> pCatalog.publishedYear)
	    	.as(dbCatalogDetailMapping *)
	    if (res==null || res.size==0) None else Some(res)
	  }
	}
	
	def deleteCatalog(pSeqNo: Int) = {
	  DB.withConnection { implicit c =>
	  	SQL("delete from CATALOG where seqno={seqno}").on('seqno -> pSeqNo
	  			).executeUpdate()
	  }
	}
	
	def softDeleteUser(pSeqNo: Int)(implicit pOfficerUserID: String) = {
	  DB.withConnection { implicit c =>
	  	SQL("update USERS " +
	  		"set is_deleted='Y', " +
	  		"audit_usercode={auditUsercode}, " +
	  		"audit_timestamp=now() " +
	  		"where seqNo={seqNo}")
	  	.on('seqNo -> pSeqNo,
	  	    'auditUsercode -> pOfficerUserID)
	  	.executeUpdate()
	  }
	}
	
	/**
	 * Find all books based on the catalog ID.
	 * 
	 * @param pCatalogID Catalog ID.
	 * @return the list of books with the same Catalog ID.
	 */
	def allBooksByCatalogID(pCatalogSeqNo: Int)(implicit pIncludeDeleted: Boolean = false) = {
	  DB.withConnection { implicit c =>
	  	val list = SQL("select * from BOOK where catalog_seqno={catalogSeqNo}")
	  		.on('catalogSeqNo -> pCatalogSeqNo).as(dbBookMapping *)
	  	list
	  }
	}
	
	/**
	 * Generate an ID for a new book to be inserted.
	 * 
	 * @param pCatalogID The new book's catalog ID number.
	 * @return the ID of the new book.
	 */
	def generateNewBookID(pCatalogSeqNo: Int): Int = {
	  DB.withConnection{ implicit c => 
	    val firstRow = SQL("SELECT coalesce(max(seqno), 0) maxseqno FROM BOOK WHERE CATALOG_SEQNO={catalogSeqNo}")
	    	.on('catalogSeqNo -> pCatalogSeqNo).apply().head
	    logger.debug("firstRow = "+firstRow)
	    val dbMaxID = firstRow[Int]("maxseqno")
    	dbMaxID+1
	  }
	}
	
	/**
	 * Fetches the list of users for a particular page.
	 * 
	 * @param startIdx The starting row index.
	 * @param endIdx The ending row index.
	 * @return The list of users for a particular page.
	 */ 
	def partialUsers(startIdx: Int, endIdx: Int): (List[UserListItem], Int) = 
	  DB.withConnection { implicit c =>
  	  val list = SQL("select * from (" +
  	  					"select row_number() over(order by USERS.SEQNO) as rowIdx, " +
  	  					"	USERS.*, NATIONALITY.NAME COUNTRYNAME from USERS, NATIONALITY " +
  	  					"	where USERS.is_deleted=false AND USERS.NATIONALITY=NATIONALITY.SEQNO" +
  	  				") tempUser " +
  	  				"where rowIdx<={endIdx} and rowIdx>={startIdx} order by rowIdx")
  	  				.on('startIdx -> startIdx, 
  	  				    'endIdx -> endIdx)
  	  				.as(dbUserListMapping *)
  	  val firstRow = SQL("select COUNT(*) c from USERS where is_deleted=false").apply.head
  	  val cnt = firstRow[Long]("c")
  	  
  	  (list, cnt.toInt)
  	}
  	
	def getCountryMap: Map[String, String] = DB.withConnection { implicit c => 
	  val list = SQL("SELECT * FROM NATIONALITY")
	  	.as(dbCountryMapping *)
	  	
	  // Convert from List to Map.
	  val res = Map((list map {s => (s.code, s.desc)}) : _*)
	  logger.debug("Size of countries = "+res.size)
	  res
	}

	def getCategoriesMap: Map[String, String] = DB.withConnection { implicit c => 
	  val list = SQL("SELECT * FROM CATEGORY")
	  	.as(dbCategoryListMapping *)
	  	
	  // Convert from List to Map.
	  val res = Map((list map {s => (s.code.toString, s.name)}) : _*)
	  res
	}

	def searchCatalogs(pStr: String): List[CatalogListItem] = {
	  val key = "%"+pStr.toUpperCase()+"%"
	  DB.withConnection { implicit c => 
		val res = SQL (
		    "select row_number() over (order by SEQNO) idx, * from " +
		    "(SELECT DISTINCT * FROM CATALOG " +
		    "where upper(AUTHOR) LIKE {str} OR upper(TITLE) LIKE upper({str})) TMP ")
		    .on('str -> key)
		    .as(dbCatalogListMapping *)
		    
		if (res==null || res.size==0) {
		  List() 
		} else {
		  res
		}
	  }
	}
 
	def listAllCategories() = { 
	  DB.withConnection { implicit c => 
		val res = SQL ("select * from CATEGORY order by seqno")
		    .as(dbCategoryListMapping *)
		    
		if (res==null || res.size==0) {
		  List() 
		} else {
		  res
		}
	  }
	}
	
	/**
	 * Finding the user based on the ID.
	 * 
	 * @param pUserID The User ID.
	 * @return the User model business object.
	 */
	def findCategoryByName(pCategoryName: String): Category = {
	  DB.withConnection{ implicit c => 
	    val list = SQL("SELECT * FROM CATEGORY WHERE upper(name) = upper({categoryName})")
	    	.on('categoryName -> pCategoryName)
	    	.as(dbCategoryListMapping *)
	    if (list==null || list.size==0) {
	      logger.debug("Category "+pCategoryName+" cannot be found!")
	      throw CategoryNotFoundException(pCategoryName)
	    }
	    list(0)
	  }
	}
	
  	/**
  	 * Insert a new user.
  	 * 
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 */
	def createCategory(pCategoryName: String, pOfficerUserID: String) = {
	  DB.withConnection { implicit c => 
	    SQL("insert into CATEGORY (NAME, create_usercode, create_timestamp, audit_usercode, audit_timestamp, audit_reason) " +
	    		"values ({name}, {createUsercode}, now(), {auditUsercode}, now(), '')")
	        .on('name -> pCategoryName,
	            'createUsercode -> pOfficerUserID, 
	            'auditUsercode -> pOfficerUserID)
	        .executeUpdate()
	  }
	}
	
	def updateCategory(pSeqNo: Int, pName: String)(implicit pOfficerUserID: String) = {
	  DB.withConnection { implicit c => 
	    val sql = """
	      	update CATEGORY 
	      	set name={name},
	      		audit_usercode = {auditUsercode},
	      		audit_timestamp = now()
	      	where seqno={seqno}
	      """
	    SQL(sql)
	        .on('name -> pName,
	            'auditUsercode -> pOfficerUserID,
	            'seqno -> pSeqNo)
	        .executeUpdate()
	  }
	}

	def insertTxBorrowHD(pObj: TDBTxBorrowHD)(implicit pOfficerUserID: String) = {
	  DB.withConnection { implicit c => 
	    val sql = "insert into TX_BORROW_HD " +
	    		"(seqno, borrower_idnumber, officer_userid, remarks, " +
	    		"status, status_usercode, status_timestamp, " +
	    		"create_usercode, create_timestamp, " +
	    		"audit_usercode, audit_timestamp) " +
	    	"values " +
	    		"(default, {borrower_idnumber}, {officer_userid}, {remarks}, " +
	    		"{status}, {statusUsercode}, now(), " +
	    		" {createUserCode}, now(), " +
	    		" {auditUserCode}, now() " +
	    		") "
	    val transactionID = SQL(sql)
	    	.on('borrower_idnumber -> pObj.borrowerIDNumber, 
	    	    'officer_userid -> pOfficerUserID, 
	    	    'remarks -> pObj.remarks,
	    	    'status -> pObj.status, 
	            'statusUsercode -> pOfficerUserID, 
	            'createUserCode -> pOfficerUserID, 
	            'auditUserCode -> pOfficerUserID
	    ).executeInsert()
	    
	    val sqlID = SQL("SELECT CURRVAL('TX_BORROW_HD_SEQ') currentID")
	    	.apply().head[Long]("currentID")
	    sqlID.toInt
	  }
	}
	
	def updateTxBorrowHD(pTxBorrowHD: TDBTxBorrowHD)(implicit pOfficerUserID: String) = {
	  DB.withConnection { implicit c => 
	    SQL("update TX_BORROW_HD " +
	    		"set borrow_timestamp={borrowTimestamp}, " +
	    		"	officer_userid={officerID}, " +
	    		"	remarks={remarks}, " +
	    		"	status={status}, " +
	    		"	status_timestamp={statusTimestamp}, " +
	    		"	status_usercode={statusUsercode}, " +
	    		"	audit_usercode = {auditUsercode}, " +
	    		"	audit_timestamp = now() " +
	    		"where seqno={seqNo} ")
    	.on('seqNo -> pTxBorrowHD.seqno, 
    	    'borrowTimestamp -> pTxBorrowHD.borrowTimestamp, 
    	    'officerID -> pOfficerUserID,
    	    'remarks -> pTxBorrowHD.remarks,
    	    'status -> pTxBorrowHD.status,
    	    'statusTimestamp -> pTxBorrowHD.statusTimestamp,
    	    'statusUsercode -> pOfficerUserID,
    	    'auditUsercode -> pOfficerUserID)
    	.executeUpdate()
	  }
	}
	
	def deleteTxBorrowDetails(hdSeqNo: Int) {
	  DB.withConnection { implicit c =>
	  	SQL("delete from TX_BORROW_DT where hd_seqno={hdSeqno}")
	  		.on('hdSeqno -> hdSeqNo).executeUpdate()
	  }
	}
	
	def insertTxBorrowDT(pDetail: TDBTxBorrowDT)(implicit pOfficerUserID: String) = {
	  DB.withConnection { implicit c => 
	    val sql = "insert into TX_BORROW_DT " +
	    		"(hd_seqno, catalog_seqno, book_seqno, " +
	    		"status, status_usercode, status_timestamp, " +
	    		"officer_userid, " +
	    		"create_usercode, create_timestamp, " +
	    		"audit_usercode, audit_timestamp) " +
	    	"values " +
	    		"({hdSeqno}, {catalogSeqNo}, {bookSeqNo}, " +
	    		"{status}, {statusUsercode}, {statusTimestamp}, " +
	    		"{officerUserID}, " +
	    		" {createUserCode}, {createTimestamp}, " +
	    		" {auditUserCode}, now() " +
	    		") "
	    
	    val statusTimestamp = pDetail.statusTimestamp match {
	      case null => now()
	      case x => x
	    }

	    val createTimestamp = pDetail.createTimestamp match {
	      case null => now()
	      case x => x
	    }

	    val transactionID = SQL(sql)
	    	.on('hdSeqno -> pDetail.hdSeqNo, 
	    	    'catalogSeqNo -> pDetail.catalogSeqNo, 
	    	    'bookSeqNo -> pDetail.bookSeqNo, 
	    	    'status -> pDetail.status, 
	            'statusUsercode -> pDetail.statusUsercode, 
	            'statusTimestamp ->  statusTimestamp,
	            'officerUserID -> pOfficerUserID,
	            'createUserCode -> pDetail.createUsercode, 
	            'createTimestamp ->  createTimestamp, 
	            'auditUserCode -> pOfficerUserID
	    ).executeInsert()
	  }
	}
	
	/*def updateTxBorrowDT(pDetail: TDBTxBorrowDT, pOfficerUserID: String) = {
	  DB.withConnection { implicit c => 
	    SQL("update TX_BORROW_DT " +
	    		"set borrow_timestamp={borrowTimestamp}, " +
	    		"	officer_userid={officerID}, " +
	    		"	remarks={remarks}, " +
	    		"	status={status}, " +
	    		"	status_timestamp={statusTimestamp}, " +
	    		"	status_usercode={statusUsercode}, " +
	    		"	audit_usercode = {auditUsercode}, " +
	    		"	audit_timestamp = now() " +
	    		"where seqno={seqNo} ")
    	.on('seqNo -> pDetail.hdSeqNo, 
    	    'borrowTimestamp -> pTxBorrowHD.borrowTimestamp, 
    	    'officerID -> pOfficerUserID,
    	    'remarks -> pTxBorrowHD.remarks,
    	    'status -> pTxBorrowHD.status,
    	    'statusTimestamp -> pTxBorrowHD.statusTimestamp,
    	    'statusUsercode -> pOfficerUserID,
    	    'auditUsercode -> pOfficerUserID)
    	.executeUpdate()
	  }
	}*/

	def now(): java.util.Date = DB.withConnection { implicit c =>
  	  val firstRow = SQL("select now() as c").apply.head
  	  val currentTime = firstRow[Date]("c")
  	  currentTime
	}
	
}