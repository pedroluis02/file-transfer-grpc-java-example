package com.github.pedroluis02.grpc.filetransfer.client;

import com.github.pedroluis02.grpc.filetransfer.TransferStatus;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientTransferStreamObserverImpl implements StreamObserver<TransferStatus> {

    private final Logger logger = Logger.getLogger(ClientTransferStreamObserverImpl.class.getName());
    private final CountDownLatch latch;

    public ClientTransferStreamObserverImpl(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onNext(TransferStatus transferStatus) {
        logger.log(Level.INFO, "transfer file code={0} msg={1}", new Object[]{transferStatus.getCode(),
                transferStatus.getMessage()});
    }

    @Override
    public void onError(Throwable throwable) {
        logger.log(Level.SEVERE, throwable.getMessage());
    }

    @Override
    public void onCompleted() {
        logger.info("completed");
        latch.countDown();
    }
}
