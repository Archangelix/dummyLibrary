package common

object CommonUtil {
	def isBlank(str: String) = str==null || str.trim().isEmpty()
}