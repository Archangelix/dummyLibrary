package controllers.util

import scala.collection.mutable.Map
import scala.collection.mutable.HashMap

/**
 * Customised Session to store complex object. Play can only store String object
 * in the session.
 */
object MySession {
  
  /**
   * Every user has his / her own session.
   */
  private val sessionMap: Map[String, Map[String, Object]] = new HashMap()
  
  	/**
  	 * Add a new object into the session for a particular user.
  	 */
	def put(userID: String, key: String, value: Object) = {
	  val userMap = 
		  if (!sessionMap.contains(userID)) {
		    sessionMap += (userID -> new HashMap[String, Object]())
		    sessionMap(userID)
		  } else {
		    sessionMap(userID)
		  }
	  
	  userMap +=  (key -> value) 
	}
  
  	/**
  	 * Get a new object from the session for a particular user.
  	 */
  	def get(userID: String, key: String): Object = {
  	  val userMap = sessionMap(userID)
  	  userMap(key)
  	}
}