//package test.elasticsearch;
//
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.transport.InetSocketTransportAddress;
//import org.elasticsearch.transport.client.PreBuiltTransportClient;
//
//import com.google.common.base.Preconditions;
// 
//public class EsClientFactory {
//    private static final Log logger = LogFactory.getLog(EsClientFactory.class);
// 
//    // 是否扫描集群
//    private static boolean sniff = false;
//    // ES 集群名称
//    private static String clusterName;
//    // IP地址
//    private static String[] ips;
//    // 端口
//    private static int esPort;
// 
//    private TransportClient esClient;//ES 客户端对象
// 
//    public  EsClientFactory(String clusterName,String[] ips,int esPort) {
//        this.clusterName=clusterName;
//        this.ips=ips;
//        this.esPort=esPort;
//        init();
//    }
// 
//    /**
//     * ES 客户端连接初始化
//     *
//     * @return ES客户端对象
//     */
//    private void init() {
//        Preconditions.checkNotNull(clusterName, "es 服务clusterName未配置");
//        Preconditions.checkNotNull(ips, "es 服务ip未配置");
//        Preconditions.checkArgument(esPort > 0, "es 服务服务port未配置");
//        //设置集群的名字
//        Settings settings = Settings.builder()
//                .put("cluster.name", clusterName)
//                .put("client.transport.sniff", sniff)
//                .build();
//        //创建集群client并添加集群节点地址
//        esClient = new PreBuiltTransportClient(settings);
//        for (String ip : ips) {
//            try {
//                esClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), esPort));
//            } catch (UnknownHostException e) {
//            }
//        }
//    }
// 
//    public TransportClient getEsClient() {
//        return esClient;
//    }
// 
//    public boolean isSniff() {
//        return sniff;
//    }
// 
//    public String getClusterName() {
//        return clusterName;
//    }
// 
// 
//    public String[] getIps() {
//        return ips;
//    }
// 
// 
//    public int getEsPort() {
//        return esPort;
//    }
// 
//} 
