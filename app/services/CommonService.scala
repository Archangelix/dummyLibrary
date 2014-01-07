package services

/**
 * This object serves as a bridge between the Business layer and Database layer.
 * Within this service object the user will process all the database request. 
 * All the results returned from this layer will already be in forms of 
 * Business Object instead of Database object.
 */
object CommonService extends TCommonService {}
