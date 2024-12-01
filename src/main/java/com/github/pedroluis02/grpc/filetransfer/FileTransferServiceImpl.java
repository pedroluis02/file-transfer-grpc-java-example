package com.github.pedroluis02.grpc.filetransfer;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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
        return new StreamObserver<>() {
            private TransferStatusCode status = null;
            private String message = null;

            private OutputStream writer = null;

            @Override
            public void onNext(TransferFile transferFile) {
                try {
                    if (writer == null) {
                        final var file = "tmp-" + System.currentTimeMillis();
                        System.out.println("new file " + file);

                        writer = Files.newOutputStream(
                                Path.of(file),
                                StandardOpenOption.CREATE,
                                StandardOpenOption.APPEND
                        );
                    }

                    writer.write(transferFile.getContent().toByteArray());
                    writer.flush();
                } catch (IOException e) {
                    this.onError(e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("onError");
                status = TransferStatusCode.ERROR;
                message = "error " + throwable.getMessage();

                this.onCompleted();
            }

            @Override
            public void onCompleted() {
                System.err.println("onCompleted");
                try {
                    writer.close();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }

                status = (status == null) ? TransferStatusCode.SUCCESS : status;
                message = (message == null) ? "Success" : message;

                responseObserver.onNext(TransferStatus.newBuilder()
                        .setCode(status)
                        .setMessage(message)
                        .build());
                responseObserver.onCompleted();
            }
        };
    }
}
