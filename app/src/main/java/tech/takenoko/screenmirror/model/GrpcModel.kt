package tech.takenoko.screenmirror.model

import android.content.IntentSender
import com.google.protobuf.ByteString
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tech.takenoko.screenmirror.model.io.NetworkProtocol
import tech.takenoko.screenmirror.proto.ImageDomainGrpc
import tech.takenoko.screenmirror.proto.ImageReply
import tech.takenoko.screenmirror.proto.ImageRequest
import tech.takenoko.screenmirror.utils.MLog

class GrpcModel() : NetworkProtocol {

    lateinit var channel: ManagedChannel
    lateinit var stub: ImageDomainGrpc.ImageDomainStub
    private var isFinished = true

    override fun connect() {
        MLog.info(TAG, "connect")
        channel = ManagedChannelBuilder.forTarget(uri).usePlaintext(true).build()
        stub = ImageDomainGrpc.newStub(channel) //newBlockingStub(channel)
    }

    override fun close() {
        MLog.info(TAG, "close")
        channel.shutdown()
    }

    override fun send(bytes: ByteArray?) {
        GlobalScope.launch(Dispatchers.IO) {
            MLog.info(TAG, "send")
            val request = ImageRequest.newBuilder().setImage(ByteString.copyFrom(bytes)).build()
            if (isOpen() && isFinished) {
                isFinished = false
                stub.getImage(request, obserber)
            }
        }
    }

    override fun send(str: String?) {
        GlobalScope.launch(Dispatchers.IO) {
            MLog.info(TAG, "send")
            val request =
                ImageRequest.newBuilder().setImage(ByteString.copyFrom(str?.toByteArray())).build()
            if (isOpen())  stub.getImage(request, obserber)
        }
    }

    override fun isOpen(): Boolean {
        MLog.info(TAG, "isOpen ${!channel.isShutdown}")
        return !channel.isShutdown
    }

    val obserber: StreamObserver<ImageReply> = object : StreamObserver<ImageReply> {
        override fun onNext(value: ImageReply?) {
            MLog.info(TAG, "onNext")
        }
        override fun onError(t: Throwable?) {
            MLog.info(TAG, "onError ${t}")
        }
        override fun onCompleted() {
            MLog.info(TAG, "onCompleted")
            isFinished = true
        }

    }

    companion object {
        val TAG: String = GrpcModel::class.java.simpleName
        var uri = "10.0.2.2:50051"
    }
}