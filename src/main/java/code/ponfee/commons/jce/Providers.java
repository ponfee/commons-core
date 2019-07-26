package code.ponfee.commons.jce;

import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * security providers
 * there has not any method defined except a static method
 * @author fupf
 */
@SuppressWarnings("restriction")
public interface Providers {

    static Provider get(String name) {
        return ProvidersHolder.NAME_HOLDER.get(name);
    }

    static Provider get(Class<? extends Provider> type) {
        Provider provider = ProvidersHolder.CLASS_HOLDER.get(type);
        if (provider != null) {
            return provider;
        }

        try {
            provider = type.getDeclaredConstructor().newInstance();
            Security.addProvider(provider);
            ProvidersHolder.NAME_HOLDER.put(provider.getName(), provider);
        } catch (Exception ignored) {
            provider = NullProvider.INSTANCE;
            ignored.printStackTrace();
        }
        ProvidersHolder.CLASS_HOLDER.put(type, provider);
        return provider;
    }

    // ----------------------------------------------------------
    static void set(Provider provider) {
        ProvidersHolder.CURRENT_PROVIDER.set(provider);
    }

    static void clear() {
        ProvidersHolder.CURRENT_PROVIDER.remove();
    }

    static void setGlobal(Provider provider) {
        ProvidersHolder.globalProvider = provider;
    }

    static void clearGlobal() {
        ProvidersHolder.globalProvider = null;
    }

    // ----------------------------------------------------------
    static KeyAgreement getKeyAgreement(String algorithm) {
        return getKeyAgreement(algorithm, null);
    }

    static KeyAgreement getKeyAgreement(String algorithm, Provider provider) {
        provider = ProvidersHolder.getProvider(provider);
        try {
            return provider == null 
                 ? KeyAgreement.getInstance(algorithm) 
                 : KeyAgreement.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    static KeyGenerator getKeyGenerator(String algorithm) {
        return getKeyGenerator(algorithm, null);
    }

    static KeyGenerator getKeyGenerator(String algorithm, Provider provider) {
        provider = ProvidersHolder.getProvider(provider);
        try {
            return provider == null 
                 ? KeyGenerator.getInstance(algorithm) 
                 : KeyGenerator.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    static Cipher getCipher(String algorithm) {
        return getCipher(algorithm, null);
    }

    static Cipher getCipher(String algorithm, Provider provider) {
        provider = ProvidersHolder.getProvider(provider);
        try {
            return provider == null 
                 ? Cipher.getInstance(algorithm) 
                 : Cipher.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new SecurityException(e);
        }
    }

    static KeyPairGenerator getKeyPairGenerator(String algorithm) {
        return getKeyPairGenerator(algorithm, null);
    }

    static KeyPairGenerator getKeyPairGenerator(String algorithm, Provider provider) {
        provider = ProvidersHolder.getProvider(provider);
        try {
            return provider == null 
                 ? KeyPairGenerator.getInstance(algorithm) 
                 : KeyPairGenerator.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    static KeyFactory getKeyFactory(String algorithm) {
        return getKeyFactory(algorithm, null);
    }

    static KeyFactory getKeyFactory(String algorithm, Provider provider) {
        provider = ProvidersHolder.getProvider(provider);
        try {
            return provider == null 
                 ? KeyFactory.getInstance(algorithm) 
                 : KeyFactory.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    static Signature getSignature(String algorithm) {
        return getSignature(algorithm, null);
    }

    static Signature getSignature(String algorithm, Provider provider) {
        provider = ProvidersHolder.getProvider(provider);
        try {
            return provider == null 
                 ? Signature.getInstance(algorithm) 
                 : Signature.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    static KeyStore getKeyStore(String algorithm) {
        return getKeyStore(algorithm, null);
    }

    static KeyStore getKeyStore(String algorithm, Provider provider) {
        provider = ProvidersHolder.getProvider(provider);
        try {
            return provider == null 
                 ? KeyStore.getInstance(algorithm) 
                 : KeyStore.getInstance(algorithm, provider);
        } catch (KeyStoreException e) {
            throw new SecurityException(e);
        }
    }

    static TrustManagerFactory getTrustManagerFactory(String algorithm) {
        return getTrustManagerFactory(algorithm, null);
    }

    static TrustManagerFactory getTrustManagerFactory(String algorithm, Provider provider) {
        provider = ProvidersHolder.getProvider(provider);
        try {
            return provider == null 
                 ? TrustManagerFactory.getInstance(algorithm) 
                 : TrustManagerFactory.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    static KeyManagerFactory getKeyManagerFactory(String algorithm) {
        return getKeyManagerFactory(algorithm, null);
    }

    static KeyManagerFactory getKeyManagerFactory(String algorithm, Provider provider) {
        provider = ProvidersHolder.getProvider(provider);
        try {
            return provider == null 
                 ? KeyManagerFactory.getInstance(algorithm) 
                 : KeyManagerFactory.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    static SSLContext getSSLContext(String algorithm) {
        return getSSLContext(algorithm, null);
    }

    static SSLContext getSSLContext(String algorithm, Provider provider) {
        provider = ProvidersHolder.getProvider(provider);
        try {
            return provider == null 
                 ? SSLContext.getInstance(algorithm) 
                 : SSLContext.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    static CertificateFactory getCertificateFactory(String algorithm) {
        return getCertificateFactory(algorithm, null);
    }

    static CertificateFactory getCertificateFactory(String algorithm, Provider provider) {
        provider = ProvidersHolder.getProvider(provider);
        try {
            return provider == null 
                 ? CertificateFactory.getInstance(algorithm) 
                 : CertificateFactory.getInstance(algorithm, provider);
        } catch (CertificateException e) {
            throw new SecurityException(e);
        }
    }

    static SecretKeyFactory getSecretKeyFactory(String algorithm) {
        return getSecretKeyFactory(algorithm, null);
    }

    static SecretKeyFactory getSecretKeyFactory(String algorithm, Provider provider) {
        provider = ProvidersHolder.getProvider(provider);
        try {
            return provider == null 
                 ? SecretKeyFactory.getInstance(algorithm) 
                 : SecretKeyFactory.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    // ------------------------------------------------------------------------------
    // BouncyCastleProvider.PROVIDER_NAME
    Provider BC         = get(org.bouncycastle.jce.provider.BouncyCastleProvider.class);
    Provider BC_PQC     = get(org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider.class);
    Provider BC_JSSE    = get(org.bouncycastle.jsse.provider.BouncyCastleJsseProvider.class);
    Provider SUN        = get(sun.security.provider.Sun.class);
    Provider SunRsaSign = get(sun.security.rsa.SunRsaSign.class);
    Provider SunEC      = get(sun.security.ec.SunEC.class);
    Provider SunJSSE    = get(com.sun.net.ssl.internal.ssl.Provider.class);
    Provider SunJCE     = get(com.sun.crypto.provider.SunJCE.class);
    Provider SunJGSS    = get(sun.security.jgss.SunProvider.class);
    Provider SunSASL    = get(com.sun.security.sasl.Provider.class);
    Provider XMLDSig    = get(org.jcp.xml.dsig.internal.dom.XMLDSigRI.class);
    Provider SunPCSC    = get(sun.security.smartcardio.SunPCSC.class);
    Provider SunMSCAPI  = get(sun.security.mscapi.SunMSCAPI.class);

    /**
     * provider holder
     */
    final class ProvidersHolder {
        // --------------------------------------------------------------------------
        private static final Map<Class<? extends Provider>, Provider> CLASS_HOLDER = new ConcurrentHashMap<>(16);
        private static final Map<String, Provider>                    NAME_HOLDER  = new ConcurrentHashMap<>(16);
        static {
            Provider[] providers = Security.getProviders();
            if (providers != null && providers.length > 0) {
                for (Provider provider : providers) {
                    CLASS_HOLDER.put(provider.getClass(), provider);
                    NAME_HOLDER.put(provider.getName(), provider);
                }
            }
        }

        // --------------------------------------------------------------------------
        private static final ThreadLocal<Provider> CURRENT_PROVIDER = new ThreadLocal<>();

        private static Provider getProvider(Provider provider) {
            return provider != null
                ? provider
                : (provider = CURRENT_PROVIDER.get()) != null
                ? provider 
                : globalProvider;
        }

        // --------------------------------------------------------------------------
        private static Provider globalProvider = null;
    }

    /**
     * The NullProvider representing the not exists provider
     */
    final class NullProvider extends Provider {
        private static final long serialVersionUID = 7420890884380155994L;
        private static final NullProvider INSTANCE = new NullProvider();

        private NullProvider() {
            super("Null", 1.0D, "Non provider");
        }
    }

}
