package slackapp.service

import com.amazonaws.util.IOUtils
import com.google.gson.GsonBuilder
import slackapp.util.LoanPattern.using

import scala.collection.JavaConverters._

class LangCodes {
  import LangCodes._

  lazy val allData: Map[String, String] = {
    val classLoader = classOf[LangCodes].getClassLoader
    using(classLoader.getResourceAsStream(filename)) { resource =>
      val jsonStr = IOUtils.toString(resource)
      val gson    = new GsonBuilder().create

      gson.fromJson(jsonStr, classOf[java.util.Map[_, _]]).asScala.map { case (k, v) => k.toString -> v.toString }.toMap
    }
  }
  lazy val allKeys = allData.keys.toSeq

}

object LangCodes {

  private val filename = "langcode.json"

}
