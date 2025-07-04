package com.github.pedroluis02.grpc.filetransfer.server;

import com.github.pedroluis02.grpc.filetransfer.FileTransferServiceGrpc;
import com.github.pedroluis02.grpc.filetransfer.MessageInfo;
import com.github.pedroluis02.grpc.filetransfer.TransferFile;
import com.github.pedroluis02.grpc.filetransfer.TransferStatus;
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

    @Override
    public StreamObserver<TransferFile> transfer(StreamObserver<TransferStatus> responseObserver) {
        return new TransferStreamObserverImpl(responseObserver);
    }
}
