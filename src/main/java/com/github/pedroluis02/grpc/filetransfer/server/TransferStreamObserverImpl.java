package com.github.pedroluis02.grpc.filetransfer.server;

import com.github.pedroluis02.grpc.filetransfer.TransferFile;
import com.github.pedroluis02.grpc.filetransfer.TransferStatus;
import com.github.pedroluis02.grpc.filetransfer.TransferStatusCode;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransferStreamObserverImpl implements StreamObserver<TransferFile> {

    private final Logger logger = Logger.getLogger(TransferStreamObserverImpl.class.getName());

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
                logger.log(Level.INFO, "new file {0}", file);

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
        logger.info("error");
        status = TransferStatusCode.ERROR;
        message = "error " + throwable.getMessage();

        this.onCompleted();
    }

    @Override
    public void onCompleted() {
        logger.info("completed");
        try {
            writer.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "writer close error: {0}", e.getMessage());
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
