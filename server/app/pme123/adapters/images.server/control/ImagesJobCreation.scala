package pme123.adapters.images.server.control

import javax.inject.{Inject, Named, Singleton}

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import pme123.adapters.server.control.{JobActor, JobCreation}
import pme123.adapters.server.entity.AdaptersContext.settings.jobConfigs
import pme123.adapters.server.entity.ServiceException
import pme123.adapters.shared.JobConfig
import shared.imagesJobIdent

import scala.concurrent.ExecutionContext

@Singleton
class ImagesJobCreation @Inject()(imagesJob: ImagesProcess
                                  , @Named("actorSchedulers") val actorSchedulers: ActorRef
                                  , system: ActorSystem
                                 )(implicit val mat: Materializer
                                   , val ec: ExecutionContext)
  extends JobCreation {

  lazy val imagesJobRef: ActorRef = system.actorOf(JobActor.props(jobConfigs(imagesJobIdent), imagesJob))

  def createJobActor(jobConfig: JobConfig): ActorRef = jobConfig.jobIdent match {
    case "imagesJob" => imagesJobRef
    case other => throw ServiceException(s"There is no Job for $other")
  }
}

