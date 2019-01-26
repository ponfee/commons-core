//package test.elasticsearch;
//
//import java.lang.reflect.Field;
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.commons.lang3.StringUtils;
//import org.apache.poi.ss.usermodel.DateUtil;
//import org.elasticsearch.action.search.SearchRequestBuilder;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.action.search.SearchType;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.aggregations.Aggregation;
//import org.elasticsearch.search.aggregations.AggregationBuilders;
//import org.elasticsearch.search.aggregations.bucket.terms.Terms;
//import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
//import org.elasticsearch.search.aggregations.metrics.avg.Avg;
//import org.elasticsearch.search.aggregations.metrics.sum.Sum;
//import org.elasticsearch.search.sort.SortOrder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.fasterxml.jackson.dataformat.yaml.snakeyaml.introspector.PropertyUtils;
//import com.google.gson.Gson;
// 
//public class EsDbUtils <T>{
//    private static final Logger logger = LoggerFactory.getLogger(EsDbUtils.class);
//    private static final int FromIndex = 0;
//    private static final int MinSize = 100;
//    private static final int MaxSize = 100000;
//    private static final int GroupMinSize = 100;
//    private static final int GroupMaxSize = 500;
//    private EsClientFactory esClientFactory;//ES 客户端工厂类
// 
//    /**
//     * 根据过滤条件查询出数据列表.需要传递索引和表名
//     * @param esQueryObj ES查询对象
//     * @param targetClass ES结果需要转换的类型
//     * @return
//     */
//    public List<? extends Object> queryObjectListByFilterQuery(EsQueryObj esQueryObj,Class targetClass) throws Exception {
//        validationEsQuery(esQueryObj);
//        List<Object> esRecords = new ArrayList<Object>();
//        long startCountTime = System.currentTimeMillis();
//        //创建ES查询Request对象
//        SearchRequestBuilder esSearch= esClientFactory.getEsClient().prepareSearch(esQueryObj.getIndexName());
//        esSearch.setTypes(esQueryObj.getTypeName())
//                .setSearchType(SearchType.QUERY_THEN_FETCH)
//                .setFrom((esQueryObj.getFromIndex() > 0) ? esQueryObj.getFromIndex() : FromIndex)
//                .setSize((0 <esQueryObj.getSize() && esQueryObj.getSize() <= MaxSize) ? esQueryObj.getSize() : MinSize);
//        //添加查询条件
//        if(esQueryObj.getAndFilterBuilder()!=null){
//            esSearch.setQuery(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), esQueryObj.getAndFilterBuilder()));
//        }
//        //添加多级排序
//        if(esQueryObj.getSortField()!=null) {
//            for (Map.Entry<String, SortOrder> entry : esQueryObj.getSortField().entrySet()) {
//                esSearch.addSort(entry.getKey(), entry.getValue());
//            }
//        }
//        //执行查询
//        SearchResponse response =esSearch .execute().actionGet();
//        for (SearchHit hit : response.getHits()) {
//            Object t = mapResult(hit.sourceAsMap(), targetClass);
//            esRecords.add(t);
//        }
//        logger.info("queryObjectListByFilterQuery search " + response.getHits().getTotalHits() + " data ,esQueryObj=" + new Gson().toJson(esQueryObj)+"-----------------------------------------! use " + (System.currentTimeMillis() - startCountTime) + " ms.");
//        return esRecords;
//    }
// 
//    /**
//     * 根据过滤条件和分组键SUM/AVG键获取分组结果(目前分组结果不支持LIMIT操作)
//     * @param esQueryObj ES查询对象
//     * @param targetClass ES结果需要转换的类型
//     * @return
//     * @throws Exception
//     */
//    public List<? extends Object> queryObjectListByFilterQueryWithAgg(EsQueryObj esQueryObj,Class targetClass) throws Exception {
//        validationEsGroupQuery(esQueryObj);
//        List<Object> esRecords = new ArrayList<Object>();
//        long startCountTime = System.currentTimeMillis();
//        if( esQueryObj.getSumFields()==null){
//            esQueryObj.setSumFields(new ArrayList<String>());
//        }
//        if(esQueryObj.getAvgFields()==null){
//            esQueryObj.setAvgFields(new ArrayList<String>());
//        }
//        TermsBuilder agg = getEsAgg(esQueryObj);
//        //创建ES查询Request对象
//        SearchRequestBuilder esSearch= esClientFactory.getEsClient().prepareSearch(esQueryObj.getIndexName());
//        esSearch.setTypes(esQueryObj.getTypeName())
//                .setSearchType(SearchType.QUERY_THEN_FETCH)
//                .addAggregation(agg);
//        //添加查询条件
//        if(esQueryObj.getAndFilterBuilder()!=null){
//            esSearch.setQuery(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), esQueryObj.getAndFilterBuilder()));
//        }
//        //添加多级排序
//        if(esQueryObj.getSortField()!=null) {
//            for (Map.Entry<String, SortOrder> entry : esQueryObj.getSortField().entrySet()) {
//                esSearch.addSort(entry.getKey(), entry.getValue());
//            }
//        }
//        //执行查询
//        SearchResponse response =esSearch .execute().actionGet();
//        List<Map<String, Object>> aggMaps= getAggMap(response,esQueryObj.getGroupFields(), esQueryObj.getSumFields(), esQueryObj.getAvgFields());
//        for(Map<String, Object> aggMap : aggMaps){
//            Object t = mapResult(aggMap, targetClass);
//            esRecords.add(t);
//        }
//        logger.info("queryObjectListByFilterQuery search " + response.getHits().getTotalHits() + " data,esQueryObj=" + new Gson().toJson(esQueryObj)+"-----------------------------------------! use " + (System.currentTimeMillis() - startCountTime) + " ms.");
//        return esRecords;
//    }
//    /**
//     * 根据分组键和SUM/AVG键组合AGG条件
//     * @param esQueryObj
//     * @return
//     */
//    private TermsBuilder getEsAgg(EsQueryObj esQueryObj) throws Exception{
//        List<String> groupFields= esQueryObj.getGroupFields();
//        List<String> sumFields= esQueryObj.getSumFields();
//        List<String> avgFields= esQueryObj.getAvgFields();
//        int groupSize=esQueryObj.getGroupSize();
//        Map<String, SortOrder> groupSortMap=esQueryObj.getGroupSortField();
//        TermsBuilder termsBuilder = AggregationBuilders.terms(groupFields.get(0)).field(groupFields.get(0));
//        if (groupFields.size() == 1) {
//            //设置排序后最后一层的结果数目
//            termsBuilder.size((0 <groupSize && groupSize <= GroupMaxSize) ? groupSize : GroupMinSize);
//            //添加group排序字段
//            if(groupSortMap!=null) {
//                List<Terms.Order> termsOrders=new ArrayList<Terms.Order>();
//                for (Map.Entry<String, SortOrder> entry : groupSortMap.entrySet()) {
//                    if(entry.getValue().equals(SortOrder.ASC)){
//                        termsOrders.add(Terms.Order.aggregation(entry.getKey(),true));
//                    }else{
//                        termsOrders.add(Terms.Order.aggregation(entry.getKey(), false));
//                    }
//                }
//                termsBuilder.order(Terms.Order.compound(termsOrders));
//            }
//            for (String avgField : avgFields) {
//                termsBuilder.subAggregation(AggregationBuilders.avg(avgField).field(avgField));
//            }
//            for (String sumField : sumFields) {
//                termsBuilder.subAggregation(AggregationBuilders.sum(sumField).field(sumField));
//            }
//        } else {
//            termsBuilder.subAggregation(getChildTermsBuilder(groupFields, 1, sumFields, avgFields,groupSize,groupSortMap));
//            //设置最外层分组量
//            termsBuilder.size(GroupMaxSize);
//        }
//        return termsBuilder;
//    }
// 
//    /**
//     * 通过递归的方式获取bucket agg分组语句
//     * @param groupFields
//     * @param i
//     * @param sumFields
//     * @param avgFields
//     * @return
//     */
//    private  TermsBuilder getChildTermsBuilder(List<String> groupFields,int i,List<String> sumFields, List<String> avgFields,int groupSize,Map<String, SortOrder> groupSortMap){
//        if(i+1==groupFields.size()){
//            TermsBuilder termsBuilderLast = AggregationBuilders.terms(groupFields.get(i)).field(groupFields.get(i));
//            //设置排序后最后一层的结果数目
//            termsBuilderLast.size((0 <groupSize && groupSize <= GroupMaxSize) ? groupSize : GroupMinSize);
//            //添加group排序字段
//            if(groupSortMap!=null) {
//                for (Map.Entry<String, SortOrder> entry : groupSortMap.entrySet()) {
//                    if(entry.getValue().equals(SortOrder.ASC)){
//                        termsBuilderLast.order(Terms.Order.aggregation(entry.getKey(),true));
//                    }else{
//                        termsBuilderLast.order(Terms.Order.aggregation(entry.getKey(),false));
//                    }
// 
//                }
//            }
//            for (String avgField : avgFields) {
//                termsBuilderLast.subAggregation(AggregationBuilders.avg(avgField).field(avgField));
//            }
//            for (String sumField : sumFields) {
//                termsBuilderLast.subAggregation(AggregationBuilders.sum(sumField).field(sumField));
//            }
//            return termsBuilderLast;
//        }
//        else{
//            TermsBuilder termsBuilder= AggregationBuilders.terms(groupFields.get(i)).field(groupFields.get(i));
//            //设置最外层分组量
//            termsBuilder.size(GroupMaxSize);
//            return  termsBuilder.subAggregation(getChildTermsBuilder(groupFields,i+1,sumFields,avgFields,groupSize,groupSortMap));
//        }
//    }
// 
//    /**
//     * 根据汇总键和SUM/AVG键，组合返回的查询值为MAP格式
//     * @param response
//     * @param groupFields
//     * @param sumFields
//     * @param avgFields
//     * @return
//     */
//    private   List<Map<String, Object>>  getAggMap(SearchResponse response,List <String>groupFields,List<String> sumFields, List<String> avgFields){
// 
//        List<Map<String, Object>> aggMaps = new ArrayList<Map<String, Object>>();
//        //首先获取最外层的AGG结果
//        Terms tempAggregation = response.getAggregations().get(groupFields.get(0));
//        //只有一个分组键不用进行递归
//        if(groupFields.size()==1){
//            for(Terms.Bucket tempBk:tempAggregation.getBuckets()){
//                Map<String, Object> tempMap = new HashMap<String, Object>();
//                tempMap.put(tempAggregation.getName(), tempBk.getKey());
//                for (Map.Entry<String, Aggregation> entry : tempBk.getAggregations().getAsMap().entrySet()) {
//                    String key = entry.getKey();
//                    if (sumFields.contains(key)) {
//                        Sum aggSum = (Sum) entry.getValue();
//                        double value = aggSum.getValue();
//                        tempMap.put(key, value);
//                    }
//                    if (avgFields.contains(key)) {
//                        Avg aggAvg = (Avg) entry.getValue();
//                        double value = aggAvg.getValue();
//                        tempMap.put(key, value);
//                    }
//                }
//                aggMaps.add(tempMap);
//            }
//        }
//        else {
//            for (Terms.Bucket bk : tempAggregation.getBuckets()) {
//                //每个最外层的分组键生成一个键值对MAP
//                Map<String, Object> nkMap = new HashMap<String, Object>();
//                nkMap.put(tempAggregation.getName(), bk.getKey());
//                //通过递归的方式填充键值对MAP并加到最终的列表中
//                getChildAggMap(bk, 1, groupFields, sumFields, avgFields, nkMap, aggMaps);
//            }
//        }
//        return aggMaps;
//    }
// 
//    /**
//     * 深层递归所有的AGG返回值，组合成最终的MAP列表
//     * @param bk 每次递归的单个Bucket
//     * @param i 控制分组键列表到了哪一层
//     * @param groupFields 分组键列表
//     * @param sumFields SUM键列表
//     * @param avgFields AVG键列表
//     * @param nkMap 键值对MAP
//     * @param aggMaps 最终结果的中间值
//     * @return
//     */
//    private  List<Map<String, Object>> getChildAggMap(Terms.Bucket bk,int i,List <String>groupFields,List<String> sumFields, List<String> avgFields,Map<String, Object> nkMap,List<Map<String, Object>> aggMaps){
// 
//        if(i==groupFields.size()-1){
//            Terms tempAggregation = bk.getAggregations().get(groupFields.get(i));
//            for(Terms.Bucket tempBk:tempAggregation.getBuckets()){
//                Map<String, Object> tempMap = new HashMap<String, Object>();
//                tempMap.putAll(nkMap);
//                tempMap.put(tempAggregation.getName(), tempBk.getKey());
//                for (Map.Entry<String, Aggregation> entry : tempBk.getAggregations().getAsMap().entrySet()) {
//                    String key = entry.getKey();
//                    if (sumFields.contains(key)) {
//                        Sum aggSum = (Sum) entry.getValue();
//                        double value = aggSum.getValue();
//                        tempMap.put(key, value);
//                    }
//                    if (avgFields.contains(key)) {
//                        Avg aggAvg = (Avg) entry.getValue();
//                        double value = aggAvg.getValue();
//                        tempMap.put(key, value);
//                    }
//                }
//                aggMaps.add(tempMap);
//            }
//            return  aggMaps;
//        } else{
//            Terms tempAggregation = bk.getAggregations().get(groupFields.get(i));
//            for(Terms.Bucket tempBk:tempAggregation.getBuckets()){
//                nkMap.put(tempAggregation.getName(), tempBk.getKey());
//                getChildAggMap(tempBk, i + 1, groupFields, sumFields, avgFields, nkMap, aggMaps);
//            }
//            return  aggMaps;
//        }
//    }
// 
//    /**
//     * 将ES结果映射到指定对象
//     * @param resultMap
//     * @param cls
//     * @return
//     * @throws Exception
//     */
//    public T mapResult(Map<String, Object> resultMap, Class<T> cls) throws  Exception{
//        T result = cls.newInstance();
//        Field[] fields = cls.getDeclaredFields();
//        for (Field field : fields) {
//            Object object = resultMap.get(field.getName());
//            if (object != null) {
//                //根据几种基本类型做转换
//                if (field.getType().equals(Long.class) || field.getType().equals(long.class)) {
//                    if(object.toString().indexOf('.')>0){
//                        PropertyUtils.setProperty(result, field.getName(), Long.parseLong(object.toString().substring(0, object.toString().indexOf('.'))));
//                    }else{
//                        PropertyUtils.setProperty(result, field.getName(), Long.parseLong(object.toString()));
//                    }
//                }else if (field.getType().equals(long.class) || field.getType().equals(long.class)) {
//                    if(object.toString().indexOf('.')>0){
//                        PropertyUtils.setProperty(result, field.getName(), Long.parseLong(object.toString().substring(0, object.toString().indexOf('.'))));
//                    }else{
//                        PropertyUtils.setProperty(result, field.getName(), Long.parseLong(object.toString()));
//                    }
//                }else if (field.getType().equals(Integer.class) || field.getType().equals(Integer.class)) {
//                    if(object.toString().indexOf('.')>0){
//                        PropertyUtils.setProperty(result, field.getName(), Integer.parseInt(object.toString().substring(0, object.toString().indexOf('.'))));
//                    }else{
//                        PropertyUtils.setProperty(result, field.getName(), Integer.parseInt(object.toString()));
//                    }
//                }else if (field.getType().equals(int.class) || field.getType().equals(int.class)) {
//                    if(object.toString().indexOf('.')>0){
//                        PropertyUtils.setProperty(result, field.getName(), Integer.parseInt(object.toString().substring(0, object.toString().indexOf('.'))));
//                    }else{
//                        PropertyUtils.setProperty(result, field.getName(), Integer.parseInt(object.toString()));
//                    }
// 
//                }else if (field.getType().equals(BigDecimal.class) || field.getType().equals(BigDecimal.class)) {
//                    PropertyUtils.setProperty(result, field.getName(), BigDecimal.valueOf(Double.parseDouble(object.toString())));
//                }else if (field.getType().equals(Double.class) || field.getType().equals(Double.class)) {
//                    PropertyUtils.setProperty(result, field.getName(), Double.parseDouble(object.toString()));
//                }else if (field.getType().equals(double.class) || field.getType().equals(double.class)) {
//                    PropertyUtils.setProperty(result, field.getName(), Double.parseDouble(object.toString()));
//                }else if (field.getType().equals(Date.class) || field.getType().equals(Date.class)) {
//                    PropertyUtils.setProperty(result, field.getName(), DateUtil.createDate(object.toString()));
//                }else if (field.getType().equals(String.class) || field.getType().equals(String.class)) {
//                    PropertyUtils.setProperty(result, field.getName(), object);
//                }
//            }
//        }
//        return result;
//    }
// 
//    /**
//     * 验证ES查询对象
//     * @param esQueryObj
//     * @throws Exception
//     */
//    private void validationEsQuery(EsQueryObj esQueryObj) throws Exception{
//        if(StringUtils.isEmpty(esQueryObj.getIndexName())&&StringUtils.isEmpty(esQueryObj.getTypeName())){
//            throw  new Exception("please check indexName and typeName");
//        }
//    }
//    /**
//     * 验证ES查询分组对象
//     * @param esQueryObj
//     * @throws Exception
//     */
//    private void validationEsGroupQuery(EsQueryObj esQueryObj) throws Exception{
//        if(StringUtils.isEmpty(esQueryObj.getIndexName())&&StringUtils.isEmpty(esQueryObj.getTypeName())){
//            throw  new Exception("please check indexName and typeName");
//        }
//        boolean groupOrderStatus=true;
//        st:for (Map.Entry<String, SortOrder> entry : esQueryObj.getGroupSortField().entrySet()) {
//            if(!esQueryObj.getSumFields().contains(entry.getKey())&&!esQueryObj.getAvgFields().contains(entry.getKey())){
//                groupOrderStatus=false;
//                break st;
//            }
//        }
//        if(!groupOrderStatus){
//            throw  new Exception("please check groupSortField");
//        }
//        if (esQueryObj.getGroupFields().isEmpty() || esQueryObj.getGroupFields().size() <= 0 ||(esQueryObj.getSumFields().isEmpty()&&esQueryObj.getAvgFields().isEmpty())) {
//            throw  new Exception("please check groupFields and sumFields and avgFields");
//        }
//    }
//    public EsClientFactory getEsClientFactory() {
//        return esClientFactory;
//    }
// 
//    public void setEsClientFactory(EsClientFactory esClientFactory) {
//        this.esClientFactory = esClientFactory;
//    }
//}