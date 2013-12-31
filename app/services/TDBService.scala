package services

import java.security.SecureRandom
import java.sql.Connection
import java.sql.SQLException
import java.util.Date

import anorm._
import anorm.SqlParser._
import models.common.CatalogListItem
import models.common.Category
import models.common.DDBookOrigin
import models.common.DDCountry
import models.common.DDUserRoles
import models.common.UserListItem
import models.common.UserRole
import models.db.DBBook
import models.db.DBCatalog
import models.db.DBTxBorrowDT
import models.db.DBTxBorrowHD
import models.db.DBUser
import models.db.TDBBook
import models.db.TDBCatalog
import models.db.TDBTxBorrowDT
import models.db.TDBTxBorrowHD
import models.db.TDBUser
import play.api.Play.current
import play.api.db.DB
import utils.CommonUtil._

trait TDBService {
  val logger = generateLogger(this)

  /**
   * START -- All database mappings.
   */
  
  val dbBookMapping: RowParser[TDBBook] = {
    get[Int]("catalog_seqno") ~
    get[Int]("seqno") ~
    get[String]("remarks") ~
	get[String]("status") ~ 
	get[String]("status_usercode") ~
	get[Date]("status_timestamp") ~
    get[Boolean]("is_deleted") ~
    get[String]("origin") ~ 
    get[String]("create_usercode") ~ 
    get[Date]("create_timestamp") ~ 
    get[String]("audit_usercode") ~ 
    get[Date]("audit_timestamp") ~ 
    get[Option[String]]("audit_reason") map {
      case catalogSeqNo~seqNo~remarks~
      		status~statusUserCode~statusTimestamp~
      		isDeleted~origin~
      		createUsercode~createTimestamp~auditUsercode~auditTimestamp~auditReason => 
        DBBook (catalogSeqNo, Some(seqNo), remarks, 
            status,statusUserCode,statusTimestamp,
            isDeleted, origin, 
      	    createUsercode, createTimestamp, auditUsercode, auditTimestamp, auditReason)
    }
  }

  val dbCatalogListMapping: RowParser[CatalogListItem] = {
    get[Long]("idx") ~
    get[Int]("seqno") ~
    get[Int]("category_seqno") ~
    get[String]("title") ~
    get[String]("author") ~
    get[Int]("published_year") map {
      case idx~seqNo~categorySeqNo~title~author~publishedYear => 
        CatalogListItem (idx.toInt, seqNo, categorySeqNo, title, author, publishedYear)
    }
  }

  val dbCatalogDetailMapping: RowParser[TDBCatalog] = {
    get[Int]("seqno") ~
    get[Int]("category_seqno") ~
    get[String]("title") ~
    get[String]("author") ~
    get[Int]("published_year") ~
    get[Date]("arrival_date") ~ 
    get[Boolean]("is_deleted") ~
    get[String]("create_usercode") ~ 
    get[Date]("create_timestamp") ~ 
    get[String]("audit_usercode") ~ 
    get[Date]("audit_timestamp") ~ 
    get[Option[String]]("audit_reason") map {
      case seqNo~categorySeqNo~title~author~publishedYear~arrivalDate~isDeleted~
      		createUsercode~createTimestamp~auditUsercode~auditTimestamp~auditReason => 
        DBCatalog (Some(seqNo), categorySeqNo, title, author, publishedYear, arrivalDate, isDeleted, 
            createUsercode, createTimestamp, auditUsercode, auditTimestamp, auditReason)
    }
  }

  val dbUserMapping: RowParser[TDBUser] = {
	get[Int]("seqNo") ~
	get[String]("userid") ~
	get[String]("name") ~
	get[String]("address") ~
	get[Date]("dob") ~ 
	get[Boolean]("gender") ~
	get[String]("id_number") ~
	get[Int]("nationality") ~
	get[Int]("user_role_seqno") ~
	get[Boolean]("is_deleted") ~ 
    get[String]("create_usercode") ~ 
    get[Date]("create_timestamp") ~ 
    get[String]("audit_usercode") ~ 
    get[Date]("audit_timestamp") ~ 
    get[Option[String]]("audit_reason") map {
	    case seqNo~userID~name~address~dob~gender~idNumber~nationality~userRoleSeqNo~isDeleted~
	    		createUsercode~createTimestamp~auditUsercode~auditTimestamp~auditReason =>  
	    	DBUser(seqNo, userID, name, address, dob, gender, idNumber, nationality, userRoleSeqNo, isDeleted,
	    	    createUsercode, createTimestamp, auditUsercode, auditTimestamp, auditReason)
	  }
	}
	
	val dbUserListMapping: RowParser[UserListItem] = {
	  get[Long]("rowIdx") ~
	  get[Int]("seqNo") ~
	  get[String]("userID") ~
	  get[String]("name") ~
	  get[String]("address") ~
	  get[Date]("dob") ~ 
	  get[Boolean]("gender") ~
	  get[String]("id_number") ~
	  get[String]("countryname") ~
	  get[Int]("user_role_seqno") map {
	    case rowIdx~seqNo~userID~name~address~dob~gender~idNumber~nationality~userRoleSeqNo =>
	      UserListItem(Some(rowIdx), seqNo, userID, name, address, dob, gender, idNumber, nationality, 
	          UserRole(userRoleSeqNo).toString)
	  }
	}
	
	val dbOriginTypeMapping: RowParser[DDBookOrigin] = {
	  get[String]("code") ~
	  get[String]("Description") map {
	    case code~description =>
	      DDBookOrigin(code, description)
	  }
	}
	
	val dbCountryMapping: RowParser[DDCountry] = {
	  get[Int]("seqno") ~
	  get[String]("name") map {
	    case id~name =>
	      DDCountry(id.toString, name)
	  }
	}
	
	val dbUserRoleMapping: RowParser[DDUserRoles] = {
	  get[Int]("seqno") ~
	  get[String]("name") map {
	    case id~name =>
	      DDUserRoles(id.toString, name)
	  }
	}
	
  val dbCategoryListMapping: RowParser[Category] = {
    get[Int]("seqno") ~
    get[String]("name") map {
      case seqno~name => Category(seqno, name)
    }
  }

  val dbTxBorrowHDMapping: RowParser[TDBTxBorrowHD] = {
	  get[Int]("seqNo") ~
	  get[String]("borrower_idnumber") ~
	  get[Option[Date]]("borrow_timestamp") ~
	  get[String]("officer_userid") ~
	  get[Option[String]]("remarks") ~
	  get[String]("status") ~ 
	  get[String]("status_usercode") ~
	  get[Date]("status_timestamp") ~
      get[String]("create_usercode") ~ 
      get[Date]("create_timestamp") ~ 
      get[String]("audit_usercode") ~ 
      get[Date]("audit_timestamp") ~ 
      get[Option[String]]("audit_reason") map {
		  	case seqNo~borrowerID~borrowTimestamp~officerUserid~remarks~
		  		status~statusUserCode~statusTimestamp~
		  		createUsercode~createTimestamp~auditUsercode~auditTimestamp~auditReason =>
		  	DBTxBorrowHD(Some(seqNo), borrowerID, borrowTimestamp, officerUserid, remarks, 
		  	    status, statusUserCode, statusTimestamp,
		  	    createUsercode, createTimestamp, auditUsercode, auditTimestamp, auditReason)
		  }
  }
  
	val dbTxBorrowDTMapping: RowParser[TDBTxBorrowDT] = {
	  get[Int]("hd_seqNo") ~
	  get[Int]("catalog_seqno") ~
	  get[Int]("book_seqno") ~
	  get[String]("status") ~
	  get[String]("status_usercode") ~
	  get[Date]("status_timestamp") ~
      get[String]("create_usercode") ~ 
      get[Date]("create_timestamp") ~ 
      get[String]("audit_usercode") ~ 
      get[Date]("audit_timestamp") ~ 
      get[Option[String]]("audit_reason") map {
	    case hdSeqNo~catalogSeqNo~bookSeqNo~
	    	status~statusUserCode~statusTimestamp~
	    	createUsercode~createTimestamp~auditUsercode~auditTimestamp~auditReason =>
	      DBTxBorrowDT(hdSeqNo, catalogSeqNo, bookSeqNo, 
	          status, statusUserCode, statusTimestamp, 
	          createUsercode, createTimestamp, auditUsercode, auditTimestamp, auditReason)
	  }
	}

	val generateRandomPassword = {
	  val sr = new SecureRandom()
	  var bytes = new Array[Byte](6)
	  sr.nextBytes(bytes)
	  bytes.toString
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
	def findByUserID(pUserID: String)(implicit pIncludeDeleted: Boolean = false): TDBUser
	
	/**
	 * Finding the user based on the ID.
	 * 
	 * @param pUserID The User ID.
	 * @return the User model business object.
	 */
	def findUserByIDNumber(pIDNumber: String)(implicit pIncludeDeleted: Boolean = false): TDBUser
	
	/**
	 * Finding the user based on the ID.
	 * 
	 * @param pUserID The User ID.
	 * @return the User model business object.
	 */
	def findUserBySeqNo(pSeqNo: Int)(implicit pIncludeDeleted: Boolean = false): TDBUser
	
	/**
	 * Getting the password for a particular ID. The password information is stored 
	 * in a different table for security reasons.
	 * 
	 * In the future the password should be hashed.
	 * 
	 * @param pUserID The User ID.
	 * @return the password stored in the database.
	 */
	def getPassword(pUserID: String): String
	
	/**
	 * Insert a new book.
	 * 
	 * @param pCatalogID The catalog ID.
	 * @param pBookID The book ID.
	 * @param pOrigin The book origin.
	 * @param pRemarks The book remarks.
	 */
	def createBook(pBook: TDBBook)(implicit pOfficerUserID: String): Unit
	
	/**
	 * Update a book information.
	 * 
	 * @param pCatalogID The catalog ID.
	 * @param pBookID The book ID.
	 * @param pOrigin The book origin.
	 * @param pRemarks The book remarks.
	 */
	def updateBook(pBook: TDBBook)(implicit pOfficerUserID: String): Unit
	
	/**
	 * Find all books based on the catalog ID.
	 * 
	 * DUPLICATE -- TO BE REMOVED.
	 * 
	 * @param pCatalogID Catalog ID.
	 */
	def findAllBooksByCatalogID(pCatalogSeqNo: Int)
	(implicit pIncludeDeleted: Boolean = false): List[TDBBook]
	
	/**
	 * Find all books based on the catalog ID.
	 * 
	 * DUPLICATE -- TO BE REMOVED.
	 * 
	 * @param pCatalogID Catalog ID.
	 */
	def findBook(pCatalogSeqNo: Int, pBookSeqNo: Int)
		(implicit pIncludeDeleted: Boolean = false): TDBBook
	
	/**
	 * Soft-delete a book based on the book ID.
	 * 
	 * @param pID Book ID.
	 */
	def deleteBook(pCatalogSeqNo: Int, pBookSeqNo: Int)
		(implicit pOfficerUserID: String ): Unit
	
	/**
	 * Fetches the list of catalogs for a particular page with / without the books information.
	 * 
	 * @param startIdx The starting row index.
	 * @param endIdx The ending row index.
	 * @param pWithBooks The indicator whether to get the books detail as well or not.
	 * @return The list of catalogs for a particular page with / without the books information.
	 */
	def partialCatalogs(startIdx: Int, endIdx: Int, pWithBooks: Boolean): (List[CatalogListItem], Int)
  	
  	/**
  	 * Insert a new catalog.
  	 * 
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 * @param pCategory The catalog category.
  	 */
	@throws(classOf[SQLException])
	def insertCatalog(pCatalog: TDBCatalog)(implicit pOfficerUserID: String): Unit
	
  	/**
  	 * Update an existing catalog.
  	 * 
  	 * @param pID The ID of the catalog to be updated.
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 * @param pCategory The catalog category.
  	 */
	def updateCatalog(pCatalog: TDBCatalog)(implicit pOfficerUserID: String): Unit
	
  	/**
  	 * Insert a new user.
  	 * 
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 */
	def createUser(pUser: TDBUser)(implicit pOfficerUserID: String, c: Connection): Int
	
	def createUserAndPassword(pUser: TDBUser, pPassword: String)(implicit pOfficerUserID: String): Unit
	
  	/**
  	 * Update an existing user.
  	 * 
  	 * @param pID The ID of the catalog to be updated.
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 */
	def updateUser(pUser: TDBUser)(implicit pOfficerUserID: String): Unit
	
	def createPassword(pUserID: String, pPassword: String)
		(implicit pOfficerUserID: String, c: Connection): Unit
	
	def updatePassword(pUserID: String, pPassword: String)
		(implicit pOfficerUserID: String): Unit
	
	/**
	 * Fetch a catalog based on the ID. The details may or may not include books information
	 * depending on the parameter. 
	 * 
	 * @param pID The catalog ID to be fetched.
	 * @param pWithBooks The indicator whether to get the books detail as well or not.
	 * @return The queried catalog.
	 */
	def findCatalogByID(pSeqNo: Int)(implicit pIncludeDeleted: Boolean = false): TDBCatalog
	
	/**
	 * Fetch a book based on the ID. 
	 * 
	 * @param pID The book ID to be fetched.
	 * @return The queried book.
	 */
	def getBorrowTransaction(pTransactionSeqNo: Int): TDBTxBorrowHD
	
	/**
	 * Fetch a book based on the ID. 
	 * 
	 * @param pID The book ID to be fetched.
	 * @return The queried book.
	 */
	def getTransactionDetailsByID(pTransactionID: Int): List[TDBTxBorrowDT]
	
	def findPendingTxByBookID(pCatalogSeqNo: Int, pBookSeqNo: Int): TDBTxBorrowDT
	
	/**
	 * Search an existing catalog.
	 * 
	 * @param pCatalog : Catalog object to be compared.
	 * @return <code>None</code> if no duplicates are found. Otherwise it returns the
	 * list of duplicates.
	 */
	def findDuplicates(pCatalog: TDBCatalog): Option[List[TDBCatalog]]
	
	def deleteCatalog(pSeqNo: Int): Unit
	
	def softDeleteUser(pSeqNo: Int)(implicit pOfficerUserID: String): Unit
	
	/**
	 * Find all books based on the catalog ID.
	 * 
	 * @param pCatalogID Catalog ID.
	 * @return the list of books with the same Catalog ID.
	 */
	def allBooksByCatalogID(pCatalogSeqNo: Int)
		(implicit pIncludeDeleted: Boolean = false): Unit
	
	/**
	 * Generate an ID for a new book to be inserted.
	 * 
	 * @param pCatalogID The new book's catalog ID number.
	 * @return the ID of the new book.
	 */
	def generateNewBookID(pCatalogSeqNo: Int): Int
	
	/**
	 * Fetches the list of users for a particular page.
	 * 
	 * @param startIdx The starting row index.
	 * @param endIdx The ending row index.
	 * @return The list of users for a particular page.
	 */ 
	def partialUsers(startIdx: Int, endIdx: Int): (List[UserListItem], Int)
  	
	def getCountryMap: Map[String, String]

	def getCategoriesMap: Map[String, String]

	def searchCatalogs(pStr: String): List[CatalogListItem]
 
	def listAllCategories(): List[Category]
	
	/**
	 * Finding the user based on the ID.
	 * 
	 * @param pUserID The User ID.
	 * @return the User model business object.
	 */
	def findCategoryByName(pCategoryName: String): Category
	
  	/**
  	 * Insert a new user.
  	 * 
  	 * @param pTitle The title.
  	 * @param pAuthor The author.
  	 * @param pPublishedYear The published year.
  	 */
	def createCategory(pCategoryName: String, pOfficerUserID: String): Unit
	
	def updateCategory(pSeqNo: Int, pName: String)
		(implicit pOfficerUserID: String): Unit
		
	def insertTxBorrowHD(pObj: TDBTxBorrowHD)
		(implicit pOfficerUserID: String): Int
	
	def updateTxBorrowHD(pTxBorrowHD: TDBTxBorrowHD)
		(implicit pOfficerUserID: String): Unit
	
	def deleteTxBorrowDetails(hdSeqNo: Int): Unit
	
	def insertTxBorrowDT(pDetail: TDBTxBorrowDT)
		(implicit pOfficerUserID: String): Unit
	
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

	def now(): java.util.Date
	
  def isBlank(str: String) = str==null || str.trim().equals("")
}