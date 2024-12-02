package com.github.pedroluis02.grpc.filetransfer;

import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

public class ClientTransferStreamObserverImpl implements StreamObserver<TransferStatus> {

    private final CountDownLatch latch;

    public ClientTransferStreamObserverImpl(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onNext(TransferStatus transferStatus) {
        System.out.println(String.format("transfer file: code=%s msg=%s", transferStatus.getCode(),
                transferStatus.getMessage()));
    }

    @Override
    public void onError(Throwable throwable) {
        System.err.println("onError: " + throwable.getMessage());
    }

    @Override
    public void onCompleted() {
        System.out.println("onCompleted");
        latch.countDown();
    }
}
