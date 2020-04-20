package test;

// 动态规划，https://blog.csdn.net/qq_32782339/article/details/100836449
public class ThrowEggsTest {

    public int countMinSetp(int egg, int num) {
        if (egg < 1 || num < 1) return 0;
        int[][] f = new int[egg + 1][num + 1];//代表egg个鸡蛋，从num楼层冷下来所需的最小的次数
        for (int i = 1; i <= egg; i++) {
            for (int j = 1; j <= num; j++)
                f[i][j] = j;//初始化，最坏的步数
        }

        for (int n = 2; n <= egg; n++) {
            for (int m = 1; m <= num; m++) {
                for (int k = 1; k < m; k++) {
                    //这里的DP的递推公式为f[n][m] = 1+max(f[n-1][k-1],f[n][m-k]) k属于[1,m-1]
                    //从1-m层中随机抽出来一层k
                    //如果第一个鸡蛋在k层碎了，那么我们将测试1~k-1层，就可以找出来，也即1+f[1][k-1]
                    //如果第一个鸡蛋在k层没有碎，那么我们将测试k+1~m也即m-k层，
                    //      这里也是重点！！！！ 
                    //      现在我们手里有2个鸡蛋，要测试m-k层，那么我想问，此时和你手里有2个鸡蛋要测试1~m-k层有什么区别？
                    //      没有区别！是的在已知k层不碎的情况下，测试k+1~m层的方法和测试1~m-k没区别，所以可以写成 1+f[n][m-k] 其中1表示为 在k层的那一次测试
                    f[n][m] = Math.min(f[n][m], 1 + Math.max(f[n - 1][k - 1], f[n][m - k]));
                }
            }
        }
        return f[egg][num];
    }

    public static void main(String[] args) {
        ThrowEggsTest e = new ThrowEggsTest();
        System.out.println(e.countMinSetp(2, 100));
    }

}
