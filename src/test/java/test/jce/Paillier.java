package test.jce;

/** 
* This program is free software: you can redistribute it and/or modify it 
* under the terms of the GNU General Public License as published by the Free 
* Software Foundation, either version 3 of the License, or (at your option) 
* any later version. 
* 
* This program is distributed in the hope that it will be useful, but WITHOUT 
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
* FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
* more details. 
* 
* You should have received a copy of the GNU General Public License along with 
* this program. If not, see <http://www.gnu.org/licenses/>. 
*/

import java.math.*;
import java.util.*;

/** 
 * Paillier Cryptosystem <br> 
 * <br> 
 * References: <br> 
 * [1] Pascal Paillier, 
 * "Public-Key Cryptosystems Based on Composite Degree Residuosity Classes," 
 * EUROCRYPT'99. URL: 
 * <a href="http://www.gemplus.com/smart/rd/publications/pdf/Pai99pai.pdf">http: 
 * //www.gemplus.com/smart/rd/publications/pdf/Pai99pai.pdf</a><br> 
 * 
 * [2] Paillier cryptosystem from Wikipedia. URL: 
 * <a href="http://en.wikipedia.org/wiki/Paillier_cryptosystem">http://en. 
 * wikipedia.org/wiki/Paillier_cryptosystem</a> 
 *  
 * @author Kun Liu (kunliu1@cs.umbc.edu) 
 * @version 1.0 
 */
public class Paillier {

    /** 
     * p and q are two large primes. lambda = lcm(p-1, q-1) = 
     * (p-1)*(q-1)/gcd(p-1, q-1). 
     */
    private BigInteger p, q, lambda;
    /** 
     * n = p*q, where p and q are two large primes. 
     */
    public BigInteger n;
    /** 
     * nsquare = n*n 
     */
    public BigInteger nsquare;
    /** 
     * a random integer in Z*_{n^2} where gcd (L(g^lambda mod n^2), n) = 1. 
     */
    private BigInteger g;
    /** 
     * number of bits of modulus 
     */
    private int bitLength;

    /** 
     * Constructs an instance of the Paillier cryptosystem. 
     *  
     * @param bitLengthVal 
     *            number of bits of modulus 
     * @param certainty 
     *            The probability that the new BigInteger represents a prime 
     *            number will exceed (1 - 2^(-certainty)). The execution time of 
     *            this constructor is proportional to the value of this 
     *            parameter. 
     */
    public Paillier(int bitLengthVal, int certainty) {
        KeyGeneration(bitLengthVal, certainty);
    }

    /** 
     * Constructs an instance of the Paillier cryptosystem with 512 bits of 
     * modulus and at least 1-2^(-64) certainty of primes generation. 
     */
    public Paillier() {
        KeyGeneration(512, 64);
    }

    /** 
     * Sets up the public key and private key. 
     *  
     * @param bitLengthVal 
     *            number of bits of modulus. 
     * @param certainty 
     *            The probability that the new BigInteger represents a prime 
     *            number will exceed (1 - 2^(-certainty)). The execution time of 
     *            this constructor is proportional to the value of this 
     *            parameter. 
     */
    public void KeyGeneration(int bitLengthVal, int certainty) {
        bitLength = bitLengthVal;
        /* 
         * Constructs two randomly generated positive BigIntegers that are 
         * probably prime, with the specified bitLength and certainty. 
         */
        p = new BigInteger(bitLength / 2, certainty, new Random());
        q = new BigInteger(bitLength / 2, certainty, new Random());

        n = p.multiply(q);
        nsquare = n.multiply(n);

        g = new BigInteger("2");
        lambda = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)).divide(p.subtract(BigInteger.ONE).gcd(q.subtract(BigInteger.ONE)));
        /* check whether g is good. */
        if (g.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).gcd(n).intValue() != 1) {
            System.out.println("g is not good. Choose g again.");
            System.exit(1);
        }
    }

    /** 
     * Encrypts plaintext m. ciphertext c = g^m * r^n mod n^2. This function 
     * explicitly requires random input r to help with encryption. 
     *  
     * @param m 
     *            plaintext as a BigInteger 
     * @param r 
     *            random plaintext to help with encryption 
     * @return ciphertext as a BigInteger 
     */
    public BigInteger Encryption(BigInteger m, BigInteger r) {
        return g.modPow(m, nsquare).multiply(r.modPow(n, nsquare)).mod(nsquare);
    }

    /** 
     * Encrypts plaintext m. ciphertext c = g^m * r^n mod n^2. This function 
     * automatically generates random input r (to help with encryption). 
     *  
     * @param m 
     *            plaintext as a BigInteger 
     * @return ciphertext as a BigInteger 
     */
    public BigInteger Encryption(BigInteger m) {
        BigInteger r = new BigInteger(bitLength, new Random());
        return g.modPow(m, nsquare).multiply(r.modPow(n, nsquare)).mod(nsquare);

    }

    /** 
     * Decrypts ciphertext c. plaintext m = L(c^lambda mod n^2) * u mod n, where 
     * u = (L(g^lambda mod n^2))^(-1) mod n. 
     *  
     * @param c 
     *            ciphertext as a BigInteger 
     * @return plaintext as a BigInteger 
     */
    public BigInteger Decryption(BigInteger c) {
        BigInteger u = g.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).modInverse(n);
        return c.modPow(lambda, nsquare).subtract(BigInteger.ONE).divide(n).multiply(u).mod(n);
    }

    /** 
     * sum of (cipher) em1 and em2 
     *  
     * @param em1 
     * @param em2 
     * @return 
     */
    public BigInteger cipher_add(BigInteger em1, BigInteger em2) {
        return em1.multiply(em2).mod(nsquare);
    }

    /** 
     * main function 
     *  
     * @param str 
     *            intput string 
     */
    public static void main(String[] str) {
        /* instantiating an object of Paillier cryptosystem */
        Paillier paillier = new Paillier();
        /* instantiating two plaintext msgs */
        BigInteger m1 = new BigInteger("20");
        BigInteger m2 = new BigInteger("60");
        /* encryption */
        BigInteger em1 = paillier.Encryption(m1);
        BigInteger em2 = paillier.Encryption(m2);
        /* printout encrypted text */
        System.out.println(em1);
        System.out.println(em2);
        /* printout decrypted text */
        System.out.println(paillier.Decryption(em1).toString());
        System.out.println(paillier.Decryption(em2).toString());

        /* 
         * test homomorphic properties -> D(E(m1)*E(m2) mod n^2) = (m1 + m2) mod 
         * n 
         */
        // m1+m2,求明文数值的和  
        BigInteger sum_m1m2 = m1.add(m2).mod(paillier.n);
        System.out.println("original sum: " + sum_m1m2.toString());
        // em1+em2，求密文数值的乘  
        BigInteger product_em1em2 = em1.multiply(em2).mod(paillier.nsquare);
        System.out.println("encrypted sum: " + product_em1em2.toString());
        System.out.println("decrypted sum: " + paillier.Decryption(product_em1em2).toString());

        /* test homomorphic properties -> D(E(m1)^m2 mod n^2) = (m1*m2) mod n */
        // m1*m2,求明文数值的乘  
        BigInteger prod_m1m2 = m1.multiply(m2).mod(paillier.n);
        System.out.println("original product: " + prod_m1m2.toString());
        // em1的m2次方，再mod paillier.nsquare  
        BigInteger expo_em1m2 = em1.modPow(m2, paillier.nsquare);
        System.out.println("encrypted product: " + expo_em1m2.toString());
        System.out.println("decrypted product: " + paillier.Decryption(expo_em1m2).toString());

        //sum test  
        System.out.println("--------------------------------");
        Paillier p = new Paillier();
        BigInteger t1 = new BigInteger("21");
        System.out.println(t1.toString());
        BigInteger t2 = new BigInteger("50");
        System.out.println(t2.toString());
        BigInteger t3 = new BigInteger("50");
        System.out.println(t3.toString());
        BigInteger et1 = p.Encryption(t1);
        System.out.println(et1.toString());
        BigInteger et2 = p.Encryption(t2);
        System.out.println(et2.toString());
        BigInteger et3 = p.Encryption(t3);
        System.out.println(et3.toString());
        BigInteger sum = new BigInteger("1");
        sum = p.cipher_add(sum, et1);
        sum = p.cipher_add(sum, et2);
        sum = p.cipher_add(sum, et3);
        System.out.println("sum: " + sum.toString());
        System.out.println("decrypted sum: " + p.Decryption(sum).toString());
        System.out.println("--------------------------------");
    }
}
