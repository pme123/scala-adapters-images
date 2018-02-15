package pme123.adapters.images.server.control

import javax.inject.Inject

import akka.actor.ActorRef
import akka.stream.Materializer
import pme123.adapters.server.control.{JobProcess, LogService}
import pme123.adapters.shared.{Logger, ProjectInfo}

import scala.concurrent.{ExecutionContext, Future}

class ImagesProcess @Inject()()
                             (implicit val mat: Materializer, val ec: ExecutionContext)
  extends JobProcess
    with Logger {

  private val jobLabel: String = "Images Job"

  // 1. level of abstraction
  // **************************

  def createInfo(): ProjectInfo =
    createInfo(version.BuildInfo.version, Nil)

  // the process fakes some long taking tasks that logs its progress
  def runJob(user: String)
            (implicit logService: LogService
             , jobActor: ActorRef): Future[LogService] = {
    Future {
      logService.info("There is no Job to execute. A process will handle the Bot Conversations.")
      logService
    }
  }
}

