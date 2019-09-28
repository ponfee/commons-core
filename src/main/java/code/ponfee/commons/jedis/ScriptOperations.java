package code.ponfee.commons.jedis;

import java.util.Collections;
import java.util.List;

/**
 * redis lua script
 * 
 * @author Ponfee
 */
public class ScriptOperations extends JedisOperations {

    ScriptOperations(JedisClient jedisClient) {
        super(jedisClient);
    }

    // --------------------------------------------------------Sharding by script hashing
    /**
     * 将脚本 script 添加到脚本缓存中，但并不立即执行这个脚本。
     * 
     * @param script the script
     * @return 给定 script 的 SHA1 校验和
     */
    public String scriptLoad(String script) {
        return call(sj -> sj.getShard(script).scriptLoad(script), null);
    }

    /**
     * 执行script
     * 
     * @param script the script
     * @param keys the keys
     * @param args the args
     * @return call redis result
     */
    public Object eval(String script, List<String> keys, List<String> args) {
        return call(sj -> sj.getShard(script).eval(script, keys, args), null);
    }

    /**
     * 根据给定的 sha1 校验码，对缓存在服务器中的脚本进行求值
     * 
     * @param script the script, use in sharding hasing
     * @param sha1 the script sha1, {@linkplain #scriptLoad(String)}
     * @param keys the ops keys
     * @param args the arguments
     * @return
     */
    public Object evalsha(String script, String sha1, List<String> keys, List<String> args) {
        return call(sj -> sj.getShard(script).evalsha(sha1, keys, args), null);
    }

    // --------------------------------------------------------Sharding by key hashing
    /**
     * Eval lua script
     * 
     * @param key    the redis key
     * @param script the script
     * @param args   the args
     * @return result
     */
    public Object eval(String key, String script, List<String> args) {
        return call(sj -> sj.getShard(key).eval(script, Collections.singletonList(key), args), null);
    }

    public Object eval(byte[] key, byte[] script, List<byte[]> args) {
        return call(sj -> sj.getShard(key).eval(script, Collections.singletonList(key), args), null);
    }

}
