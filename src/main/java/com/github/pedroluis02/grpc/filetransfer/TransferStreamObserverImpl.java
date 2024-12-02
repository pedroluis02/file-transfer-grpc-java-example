package com.github.pedroluis02.grpc.filetransfer;

import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class TransferStreamObserverImpl implements StreamObserver<TransferFile> {

    private TransferStatusCode status = null;
    private String message = null;

    private OutputStream writer = null;

    private final StreamObserver<TransferStatus> responseObserver;

    public TransferStreamObserverImpl(StreamObserver<TransferStatus> responseObserver) {
        this.responseObserver = responseObserver;
    }

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
}
