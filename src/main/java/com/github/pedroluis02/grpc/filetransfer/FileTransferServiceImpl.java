package com.github.pedroluis02.grpc.filetransfer;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

public class FileTransferServiceImpl extends FileTransferServiceGrpc.FileTransferServiceImplBase {
    @Override
    public void getInfo(Empty request, StreamObserver<MessageInfo> responseObserver) {
        final var message = MessageInfo.newBuilder()
                .setMessage("FT Server v1.0")
                .build();
        responseObserver.onNext(message);
        responseObserver.onCompleted();
    }
}
