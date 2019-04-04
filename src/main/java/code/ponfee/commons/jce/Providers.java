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

    static Provider get(Class<? extends Provider> type) {
        Provider provider = ProvidersHolder.HOLDER.get(type);
        if (provider != null) {
            return provider;
        }

        try {
            provider = type.getDeclaredConstructor().newInstance();
            Security.addProvider(provider);
        } catch (Exception ignored) {
            provider = NullProvider.INSTANCE;
            ignored.printStackTrace();
        }
        ProvidersHolder.HOLDER.put(type, provider);
        return provider;
    }

    // ----------------------------------------------------------
    static KeyAgreement getKeyAgreement(String algorithm) {
        Provider current = ProvidersHolder.CURRENT_PROVIDER.get();
        try {
            return current == null 
                 ? KeyAgreement.getInstance(algorithm) 
                 : KeyAgreement.getInstance(algorithm, current);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    static KeyGenerator getKeyGenerator(String algorithm) {
        Provider current = ProvidersHolder.CURRENT_PROVIDER.get();
        try {
            return current == null 
                 ? KeyGenerator.getInstance(algorithm) 
                 : KeyGenerator.getInstance(algorithm, current);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    static Cipher getCipher(String algorithm) {
        Provider current = ProvidersHolder.CURRENT_PROVIDER.get();
        try {
            return current == null 
                 ? Cipher.getInstance(algorithm) 
                 : Cipher.getInstance(algorithm, current);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new SecurityException(e);
        }
    }

    static KeyPairGenerator getKeyPairGenerator(String algorithm) {
        Provider current = ProvidersHolder.CURRENT_PROVIDER.get();
        try {
            return current == null 
                 ? KeyPairGenerator.getInstance(algorithm) 
                 : KeyPairGenerator.getInstance(algorithm, current);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    static KeyFactory getKeyFactory(String algorithm) {
        Provider current = ProvidersHolder.CURRENT_PROVIDER.get();
        try {
            return current == null 
                 ? KeyFactory.getInstance(algorithm) 
                 : KeyFactory.getInstance(algorithm, current);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    static Signature getSignature(String algorithm) {
        Provider current = ProvidersHolder.CURRENT_PROVIDER.get();
        try {
            return current == null 
                 ? Signature.getInstance(algorithm) 
                 : Signature.getInstance(algorithm, current);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    static KeyStore getKeyStore(String algorithm) {
        Provider current = ProvidersHolder.CURRENT_PROVIDER.get();
        try {
            return current == null 
                 ? KeyStore.getInstance(algorithm) 
                 : KeyStore.getInstance(algorithm, current);
        } catch (KeyStoreException e) {
            throw new SecurityException(e);
        }
    }

    static TrustManagerFactory getTrustManagerFactory(String algorithm) {
        Provider current = ProvidersHolder.CURRENT_PROVIDER.get();
        try {
            return current == null 
                 ? TrustManagerFactory.getInstance(algorithm) 
                 : TrustManagerFactory.getInstance(algorithm, current);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    static KeyManagerFactory getKeyManagerFactory(String algorithm) {
        Provider current = ProvidersHolder.CURRENT_PROVIDER.get();
        try {
            return current == null 
                 ? KeyManagerFactory.getInstance(algorithm) 
                 : KeyManagerFactory.getInstance(algorithm, current);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    static SSLContext getSSLContext(String algorithm) {
        Provider current = ProvidersHolder.CURRENT_PROVIDER.get();
        try {
            return current == null 
                 ? SSLContext.getInstance(algorithm) 
                 : SSLContext.getInstance(algorithm, current);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    static CertificateFactory getCertificateFactory(String algorithm) {
        Provider current = ProvidersHolder.CURRENT_PROVIDER.get();
        try {
            return current == null 
                 ? CertificateFactory.getInstance(algorithm) 
                 : CertificateFactory.getInstance(algorithm, current);
        } catch (CertificateException e) {
            throw new SecurityException(e);
        }
    }

    static SecretKeyFactory getSecretKeyFactory(String algorithm) {
        Provider current = ProvidersHolder.CURRENT_PROVIDER.get();
        try {
            return current == null 
                 ? SecretKeyFactory.getInstance(algorithm) 
                 : SecretKeyFactory.getInstance(algorithm, current);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        }
    }

    // ----------------------------------------------------------
    static void set(Provider provider) {
        ProvidersHolder.CURRENT_PROVIDER.set(provider);
    }

    static void clear() {
        ProvidersHolder.CURRENT_PROVIDER.remove();
    }

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
        private static final Map<Class<? extends Provider>, Provider> HOLDER = new ConcurrentHashMap<>(16);
        static {
            Provider[] providers = Security.getProviders();
            if (providers != null && providers.length > 0) {
                for (Provider provider : providers) {
                    HOLDER.put(provider.getClass(), provider);
                }
            }
        }

        private static final ThreadLocal<Provider> CURRENT_PROVIDER = new ThreadLocal<>();
    }

    /**
     * The NullProvider representing the not exists provider
     */
    final class NullProvider extends Provider {
        private static final long serialVersionUID = 7420890884380155994L;
        private static final NullProvider INSTANCE = new NullProvider();

        private NullProvider() {
            super("Null", 1.0D, "None provider");
        }
    }

}
