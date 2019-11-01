package code.ponfee.commons.jedis.spring;

import java.security.MessageDigest;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

import code.ponfee.commons.jce.DigestAlgorithms;
import code.ponfee.commons.jce.digest.DigestUtils;
import code.ponfee.commons.math.Maths;

/**
 * Bloom filter based redis
 * 
 * @author Ponfee
 */
public class RedisBloomFilter {

    private static final DigestAlgorithms DIGEST = DigestAlgorithms.MD5;

    private long expireDays = 1;

    private final String filterKey;
    private final int sizeOfBloomFilter; // total length of the Bloom filter
    private final int expectedInsertions; // expected (maximum) number of elements to be added
    private final int numberOfHashFunctions; // number of hash functions

    private final RedisTemplate<String, byte[]> redisTemplate;

    public RedisBloomFilter(String filterKey, RedisTemplate<String, byte[]> redisTemplate) {
        this(filterKey, redisTemplate, 0.0001, 600000);
    }

    /**
     * Constructs an empty Bloom filter.
     *
     * @param filterKey the redis key.
     * @param redisTemplate the spring redis template.
     * @param m is the total length of the Bloom filter.
     * @param n is the expected number of elements the filter will contain.
     * @param k is the number of hash functions used.
     */
    public RedisBloomFilter(String filterKey, RedisTemplate<String, byte[]> redisTemplate, int m, int n, int k) {
        this.filterKey = filterKey;
        this.redisTemplate = redisTemplate;
        this.sizeOfBloomFilter = m;
        this.expectedInsertions = n;
        this.numberOfHashFunctions = k;
    }

    /**
     * Constructs an empty Bloom filter with a given false positive probability.
     * The size of bloom filter and the number of hash functions is estimated
     * to match the false positive probability.
     *
     * @param filterKey the redis key.
     * @param redisTemplate the spring redis template.
     * @param fpp is the desired false positive probability.
     * @param expectedInsertions is the expected number of elements in the Bloom filter.
     */
    public RedisBloomFilter(String filterKey, RedisTemplate<String, byte[]> redisTemplate, double fpp, int expectedInsertions) {
        this(
            filterKey,
            redisTemplate,
            (int) Math.ceil((int) Math.ceil(-Maths.log2(fpp)) * expectedInsertions / Math.log(2)), // m = ceil(kn/ln2)
            expectedInsertions, 
            (int) Math.ceil(-Maths.log2(fpp)) // k = ceil(-ln(f)/ln2)
        ); 
    }

    /**
     * Adds an array of bytes to the Bloom filter.
     *
     * @param element array of bytes to add to the Bloom filter.
     */
    public void put(byte[] element) {
        if (redisTemplate.getExpire(filterKey) == -2) {
            redisTemplate.opsForValue().setBit(filterKey, 0, false);
            redisTemplate.expire(filterKey, expireDays, TimeUnit.DAYS);
        }

        int[] hashes = createHashes(element, numberOfHashFunctions);
        for (int hash : hashes) {
            redisTemplate.opsForValue().setBit(filterKey, Math.abs(hash % sizeOfBloomFilter), true);
        }
    }

    /**
     * Adds all elements from a Collection to the Bloom filter.
     *
     * @param elements Collection of elements.
     */
    public void putAll(Collection<byte[]> elements) {
        elements.forEach(this::put);
    }

    /**
     * Returns true if the array of bytes could have been inserted into the Bloom filter.
     * Use getFalsePositiveProbability() to calculate the probability of this
     * being correct.
     *
     * @param element array of bytes to check.
     * @return true if the array could have been inserted into the Bloom filter.
     */
    public boolean mightContain(byte[] element) {
        int[] hashes = createHashes(element, numberOfHashFunctions);
        for (int hash : hashes) {
            if (!redisTemplate.opsForValue().getBit(filterKey, Math.abs(hash % sizeOfBloomFilter))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if all the elements of a Collection could have been inserted
     * into the Bloom filter. Use getFalsePositiveProbability() to calculate the
     * probability of this being correct.
     *
     * @param elements elements to check.
     * @return true if all the elements in c could have been inserted into the Bloom filter.
     */
    public boolean mightContainAll(Collection<byte[]> elements) {
        return elements.stream().allMatch(this::mightContain);
    }

    /**
     * Generates digests based on the contents of an array of bytes 
     * and splits the result into 4-byte int's and store them in an array. The
     * digest function is called until the required number of int's are produced. 
     * For each call to digest a salt is prepended to the data. 
     * The salt is increased by 1 for each call.
     *
     * @param element   specifies input data.
     * @param hashes number of hashes/int's to produce.
     * @return array of int-sized hashes
     */
    public static int[] createHashes(byte[] element, int hashes) {
        int[] result = new int[hashes];
        byte salt = 0;
        byte[] output;
        MessageDigest md = DigestUtils.getMessageDigest(DIGEST, null);
        for (int k = 0, n = DIGEST.byteSize() / 4; k < hashes;) {
            md.update(salt++);
            output = md.digest(element);
            for (int i = 0, h; i < n && k < hashes; i++) {
                h = 0;
                for (int j = i << 2, m = (i << 2) + 4; j < m; j++) {
                    h <<= 8;
                    h |= ((int) output[j]) & 0xFF;
                }
                result[k] = h;
                k++;
            }
        }
        return result;
    }

    public int getSizeOfBloomFilter() {
        return this.sizeOfBloomFilter;
    }

    public int getExpectedInsertions() {
        return expectedInsertions;
    }

    public int getNumberOfHashFunctions() {
        return this.numberOfHashFunctions;
    }

    /**
     * Compares the contents of two instances to see if they are equal.
     *
     * @param obj is the object to compare to.
     * @return True if the contents of the objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        RedisBloomFilter other = (RedisBloomFilter) obj;
        if (this.sizeOfBloomFilter != other.sizeOfBloomFilter) {
            return false;
        }
        if (this.expectedInsertions != other.expectedInsertions) {
            return false;
        }
        return this.numberOfHashFunctions == other.numberOfHashFunctions;
    }

    /**
     * Calculates a hash code for this class.
     *
     * @return hash code representing the contents of an instance of this class.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + this.sizeOfBloomFilter;
        hash = 61 * hash + this.expectedInsertions;
        hash = 61 * hash + this.numberOfHashFunctions;
        return hash;
    }

}
