package com.quorum.tessera.sync;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.ResendRequest;
import com.quorum.tessera.partyinfo.ResendRequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class TransactionRequesterImpl implements TransactionRequester {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionRequesterImpl.class);

    private final Enclave enclave;

    private final ResendClient client;

    public TransactionRequesterImpl(final Enclave enclave, final ResendClient client) {
        this.enclave = Objects.requireNonNull(enclave);
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public boolean requestAllTransactionsFromNode(final String uri) {
        return this.enclave.getPublicKeys().stream()
                .map(this::createRequestAllEntity)
                .allMatch(req -> this.makeRequest(uri, req));
    }

    /**
     * Will make the desired request until succeeds or max tries has been reached
     *
     * @param uri the URI to call
     * @param request the request object to send
     */
    private boolean makeRequest(final String uri, final ResendRequest request) {
        LOGGER.debug("Requesting a resend to {} for key {}", uri, request.getPublicKey());

        try {
            return client.makeResendRequest(uri, request);
        } catch (final Exception ex) {
            LOGGER.warn("Failed to make resend request to node {} for key {}", uri, request.getPublicKey());
            return false;
        }
    }

    /**
     * Creates the entity that should be sent to the target URL
     *
     * @param key the public key that transactions should be resent for
     * @return the request to be sent
     */
    private ResendRequest createRequestAllEntity(final PublicKey key) {

        final ResendRequest request = new ResendRequest();
        final String encoded = key.encodeToBase64();
        request.setPublicKey(encoded);
        request.setType(ResendRequestType.ALL);

        return request;
    }
}
