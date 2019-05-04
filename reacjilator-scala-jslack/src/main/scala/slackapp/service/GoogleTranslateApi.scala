package slackapp.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.translate.Translate.TranslateOption
import com.google.cloud.translate.{ Translate, TranslateOptions, Translation }
import slackapp.util.LoanPattern.using

class GoogleTranslateApi {
  import GoogleTranslateApi._

  private lazy val translateApi: Translate = {
    val classLoader = classOf[GoogleTranslateApi].getClassLoader
    using(classLoader.getResourceAsStream(filename)) { fileResource =>
      val options: TranslateOptions = {
        TranslateOptions.newBuilder
          .setCredentials(GoogleCredentials.fromStream(fileResource))
          .build
      }
      options.getService
    }
  }

  def translate(text: String, lang: String): String = {
    val translationResult: Translation = translateApi.translate(text, TranslateOption.targetLanguage(lang))
    translationResult.getTranslatedText
  }

}

object GoogleTranslateApi {

  // Replacing "google-service-account.json" under src/main/resources is expected here
  // To enable this service to run anywhere, we need to avoid using
  // the env variable "GOOGLE_APPLICATION_CREDENTIALS" pointing the filepath of a JSON file
  private val filename = "google-service-account.json"

}
