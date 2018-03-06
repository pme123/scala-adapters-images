package pme123.adapters.images.server.control

import javax.inject.{Inject, Named}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.Materializer
import pme123.adapters.images.server.control.ImagesRepo.{ImagesType, InitRepo, SwitchPage}
import pme123.adapters.server.control.{JobProcess, LogService}
import pme123.adapters.shared.{Logger, ProjectInfo}

import scala.concurrent.{ExecutionContext, Future}

class ImagesProcess @Inject()(@Named("imagesRepo")
                              imagesRepo: ActorRef)
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
    // make sure the Repo is initialized
    imagesRepo ! InitRepo(jobActor)
    // all to do is to switch the page
    (imagesRepo ? SwitchPage)
      .mapTo[ImagesType]
      .map { iType =>
        logService.info(s"The Page was switched to $iType")
        logService
      }
  }
}

