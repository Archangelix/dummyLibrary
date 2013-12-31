package models

/**
 * User security business object. This is the object used for all business processes.
 */
case class OBUserPassword (
    userID: String, 
    password: String
) extends TUserPassword
