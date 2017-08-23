package org.connectbot.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import com.trilead.ssh2.auth.SignatureManager;

import org.connectbot.bean.AgentBean;
import org.openintents.ssh.ISSHAgentService;
import org.openintents.ssh.SSHAgentApiConstants;
import org.openintents.ssh.SigningRequest;
import org.openintents.ssh.SigningResponse;
import org.openintents.ssh.client.Connection;
import org.openintents.ssh.client.SSHAgentConnector;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class SSHAgentSignatureManager extends SignatureManager {
    private static int REQUEST_CODE = 1;

    private AgentBean agentBean;
    private SSHAgentConnector agentConnector;
    private byte[] signature = null;

    /**
     * Instantiates a new SignatureManager which needs a public key for the
     * later authentication process.
     */
    private SSHAgentSignatureManager(Context context, AgentBean agentBean, PublicKey publicKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
        super(publicKey);
        this.agentConnector = new SSHAgentConnector(context, new ComponentName(agentBean.getPackageName(), agentBean.getServiceName()));
        this.agentBean = agentBean;
    }

    @Override
    public synchronized byte[] sign(final byte[] challenge, final String algorithm) throws IOException {
        // initialize lock object to wait until signing of agent is finished
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        signature = null;

        this.agentConnector.connect(new Connection.OnBound() {
            @Override
            public void onBound(ISSHAgentService sshAgent) {
                try {
                    SigningRequest signingRequest = new SigningRequest(challenge, agentBean.getKeyIdentifier(), algorithm);
                    SigningResponse signingResponse = agentConnector.sign(signingRequest);

                    switch (signingResponse.getStatusCode()) {
                        case SSHAgentApiConstants.STATUS_CODE_SUCCESS:
                            signature = signingResponse.getSignature();
                            countDownLatch.countDown();
                            return;
                        case SSHAgentApiConstants.STATUS_CODE_FAILURE:
                            // authentication failed, null will be returned
                            countDownLatch.countDown();
                            return;
                        case SSHAgentApiConstants.STATUS_CODE_USER_INTERACTION_REQUIRED:
                            final ActivityResultDispatcher resultDispatcher = ActivityResultDispatcher.getInstance();
                            resultDispatcher.registerOnActivityResultListener(new ActivityResultDispatcher.OnActivityResultListener() {
                                @Override
                                public void onActivityResult(int requestCode, int resultCode, Intent data) {
                                    if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                                        signature = data.getByteArrayExtra(SSHAgentApiConstants.EXTRA_SIGNATURE);
                                    }
                                    countDownLatch.countDown();
                                    resultDispatcher.unregisterOnActivityResultListener(this);
                                }
                            });
                            boolean success = resultDispatcher.requestStartForResult(signingResponse.getPendingIntent(), REQUEST_CODE);
                            if (!success) {
                                // No activity is registered in ActivityResultDispatcher
                                countDownLatch.countDown();
                            }
                    }
                } catch (RemoteException e) {
                    Log.d(getClass().toString(), "Error while signing key from agent:");
                    Log.d(getClass().toString(), e.getMessage());
                    e.printStackTrace();
                }
            }
            @Override
            public void onError() {
                countDownLatch.countDown();
            }
        });

        // wait until we have a signature
        try {
            countDownLatch.await(120, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return signature;
    }

    public static class Builder {
        private Context context;
        private AgentBean agentBean;

        public Builder(Context context, AgentBean agentBean) {
            this.context = context;
            this.agentBean = agentBean;
        }

        public SSHAgentSignatureManager build() throws InvalidKeySpecException, NoSuchAlgorithmException {
            PublicKey publicKey = PubkeyUtils.decodePublic(agentBean.getPublicKey(), agentBean.getKeyType());
            return new SSHAgentSignatureManager(context, agentBean, publicKey);
        }
    }
}

