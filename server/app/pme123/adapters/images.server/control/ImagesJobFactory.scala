package pme123.adapters.images.server.control

import javax.inject.{Inject, Singleton}

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import pme123.adapters.server.control.{JobActor, JobActorFactory}
import pme123.adapters.server.entity.ServiceException
import pme123.adapters.shared.JobConfig.JobIdent
import shared.imagesJobIdent

import scala.concurrent.ExecutionContext

@Singleton
class ImagesJobFactory @Inject()(imagesJob: ImagesProcess
                                 , system: ActorSystem
                              )(implicit val mat: Materializer
                                , val ec: ExecutionContext)
  extends JobActorFactory {


  private lazy val imagesJobRef = system.actorOf(JobActor.props(imagesJobIdent, imagesJob))

  def jobActorFor(jobIdent: JobIdent): ActorRef = {
    jobIdent match {
      case "imagesJob" => imagesJobRef
      case other =>
        throw ServiceException(s"There is no Job for $other")
    }
  }

}

