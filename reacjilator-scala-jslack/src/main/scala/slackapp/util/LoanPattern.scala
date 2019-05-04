package slackapp.util

import scala.language.reflectiveCalls
import scala.util.control.Exception._

trait LoanPattern {

  type Closable = { def close(): Unit }

  def using[R <: Closable, A](resource: R)(f: R => A): A = {
    try {
      f(resource)
    } finally {
      ignoring(classOf[Throwable]) apply {
        resource.close()
      }
    }
  }

}

object LoanPattern extends LoanPattern
