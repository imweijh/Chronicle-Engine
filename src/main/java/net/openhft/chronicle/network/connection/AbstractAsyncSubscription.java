package net.openhft.chronicle.network.connection;

import net.openhft.chronicle.wire.WireOut;

/**
 * Created by Rob Austin
 */
public abstract class AbstractAsyncSubscription implements AsyncSubscription {

    private final long tid;
    private final TcpChannelHub hub;
    private String csp;

    public AbstractAsyncSubscription(final TcpChannelHub hub, String csp) {
        tid = hub.nextUniqueTransaction(System.currentTimeMillis());
        this.hub = hub;
        this.csp = csp;
    }

    @Override
    public long tid() {
        return tid;
    }

    @Override
    public void applySubscribe() {
        hub.outBytesLock().lock();
        try {

            hub.writeMetaDataForKnownTID(tid(), hub.outWire(), csp, 0);
            hub.outWire().writeDocument(false, this::onSubsribe);

            hub.writeSocket(hub.outWire());
        } finally {
            hub.outBytesLock().unlock();
        }
    }

    /**
     * called when ever the  TcpChannelHub is ready to make a subscription
     *
     * @param wireOut the wire to write the subscription to
     */
    public abstract void onSubsribe(WireOut wireOut);

    /**
     * called whenever the connection to the server has been dropped
     */
    @Override
    public void onClose() {

    }
}
