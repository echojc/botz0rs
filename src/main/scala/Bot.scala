import akka.actor._
import akka.io.{ IO, Tcp }
import Tcp._
import akka.util.ByteString
import java.net.InetSocketAddress

object Bot extends App {
  implicit val system = ActorSystem()

  val bot = system.actorOf(Props(classOf[Bot], "localhost", 6667))
}

class Bot(host: String, port: Int) extends Actor {
  val nick = "jcbot"
  val user = "jc"
  val real = "jc"

  // underlying tcp client
  val client = context.actorOf(Props(classOf[TcpClient], new InetSocketAddress(host, port), self))

  def receive = connectingState

  def connectingState: Receive = {
    case _: Connected =>
      client ! ByteString(s"NICK $nick\n")
      client ! ByteString(s"USER $user 0 * :$real\n")
      context become connectedState
  }

  def connectedState: Receive = {
    case bs: ByteString => println(s"> ${bs.utf8String}")
    case x => println(s"> $x")
  }
}

/**
 * From Akka docs with a few minor tweaks
 * http://doc.akka.io/docs/akka/snapshot/scala/io-tcp.html#Connecting
 */
class TcpClient(remote: InetSocketAddress, listener: ActorRef) extends Actor {

  import context.system

  IO(Tcp) ! Connect(remote)

  def receive = {
    case CommandFailed(_: Connect) ⇒
      listener ! "failed"
      context stop self

    case c @ Connected(remote, local) ⇒
      listener ! c
      val connection = sender
      connection ! Register(self)
      context become {
        case data: ByteString        ⇒ println(s"< ${data.utf8String}"); connection ! Write(data)
        case CommandFailed(w: Write) ⇒ println("!!! WRITE FAILED !!!")// O/S buffer was full
        case Received(data)          ⇒ listener ! data
        case "close"                 ⇒ connection ! Close
        case _: ConnectionClosed     ⇒ context stop self
      }
  }
}
