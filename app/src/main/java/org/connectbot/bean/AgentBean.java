package org.connectbot.bean;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import org.connectbot.R;
import org.connectbot.util.AgentDatabase;

public class AgentBean extends AbstractBean{
    public static final String BEAN_NAME = "agent";

    private long id = -1;
    private String keyIdentifier;
    private String serviceName;
    private String keyType;

    public AgentBean() {

    }

    public AgentBean(String keyIdentifier, String keyType , String serviceName, String packageName, byte[] publicKey) {
        this.keyIdentifier = keyIdentifier;
        this.keyType = keyType;
        this.serviceName = serviceName;
        this.packageName = packageName;
        this.publicKey = publicKey;
    }

    private String packageName;
    private byte[] publicKey;

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKeyIdentifier() {
        return keyIdentifier;
    }

    public void setKeyIdentifier(String keyIdentifier) {
        this.keyIdentifier = keyIdentifier;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public byte[] getPublicKey() {
        if (publicKey == null)
            return null;
        else
            return publicKey.clone();
    }

    public void setPublicKey(byte[] encoded) {
        if (encoded == null)
            publicKey = null;
        else
            publicKey = encoded.clone();
    }

    public String getAgentAppName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
        } catch (final PackageManager.NameNotFoundException e) {
            return context.getString(R.string.Unknown);
        }
        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : context.getString(R.string.Unknown));
    }

    @Override
    public ContentValues getValues() {

        ContentValues values = new ContentValues();

        values.put(AgentDatabase.FIELD_AGENT_KEY_IDENTIFIER, keyIdentifier);
        values.put(AgentDatabase.FIELD_AGENT_KEY_TYPE, keyType);
        values.put(AgentDatabase.FIELD_AGENT_PACKAGE_NAME, packageName);
        values.put(AgentDatabase.FIELD_AGENT_SERVICE_NAME, serviceName);
        values.put(AgentDatabase.FIELD_AGENT_PUBLIC_KEY, publicKey);

        return values;
    }

    @Override
    public String getBeanName() {
        return BEAN_NAME;
    }
}
