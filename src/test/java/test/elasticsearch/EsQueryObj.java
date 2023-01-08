//package test.elasticsearch;
//
//import java.util.List;
//import java.util.Map;
//
//import org.elasticsearch.index.query.AndFilterBuilder;
//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.search.sort.SortOrder;
// 
//public class EsQueryObj {
//    /**
//     * ES索引名
//     */
//    String indexName;
//    /**
//     * ES TYPE名（表名)
//     */
//    String typeName;
//    /**
//     * 查询条件组合，类似于 "boolQuery().must( QueryBuilders.termQuery("opTime", "2016-03-30")).must(QueryBuilders.termQuery("operErpID", "xingkai"))"
//     */
//    BoolQueryBuilder bq;
//    /**
//     * 查询条件组合，类似于 "boolQuery().must( QueryBuilders.termQuery("opTime", "2016-03-30")).must(QueryBuilders.termQuery("operErpID", "xingkai"))"
//     */
//    AndFilterBuilder andFilterBuilder;
//    /**
//     * 分组键值列表，类似于group by 之后的字段
//     */
//    List <String> groupFields;
//    /**
//     * 分组SUM键值列表
//     */
//    List <String> sumFields;
//    /**
//     * 分组AVG键值列表
//     */
//    List <String> avgFields;
//    /**
//     * 排序字段键值对，可以添加多个，类似于("opTime","DESC")
//     */
//    Map<String,SortOrder> sortField;
//    /**
//     * 分组后排序字段键值对，可以添加多个，类似于("opTime","DESC"),注意此处最后PUT的键最先排序,排序键必须在sumFields或avgFields(只针对最后一层分组)
//     */
//    Map<String,SortOrder> groupSortField;
//    /**
//     * 分组后返回数据数量，默认为100，最大不能超过500(只针对最后一层分组)
//     */
//    int groupSize;
//    /**
//     * 取值的起始位置，默认为0
//     */
//    int fromIndex;
//    /**
//     * 返回数据数量，默认为100，最大不能超过100000
//     */
//    int size;
// 
//    public String getIndexName() {
//        return indexName;
//    }
// 
//    public void setIndexName(String indexName) {
//        this.indexName = indexName;
//    }
// 
//    public String getTypeName() {
//        return typeName;
//    }
// 
//    public void setTypeName(String typeName) {
//        this.typeName = typeName;
//    }
// 
//    public BoolQueryBuilder getBq() {
//        return bq;
//    }
// 
//    public void setBq(BoolQueryBuilder bq) {
//        this.bq = bq;
//    }
// 
//    public AndFilterBuilder getAndFilterBuilder() {
//        return andFilterBuilder;
//    }
// 
//    public void setAndFilterBuilder(AndFilterBuilder andFilterBuilder) {
//        this.andFilterBuilder = andFilterBuilder;
//    }
// 
//    public List<String> getGroupFields() {
//        return groupFields;
//    }
// 
//    public void setGroupFields(List<String> groupFields) {
//        this.groupFields = groupFields;
//    }
// 
//    public List<String> getSumFields() {
//        return sumFields;
//    }
// 
//    public void setSumFields(List<String> sumFields) {
//        this.sumFields = sumFields;
//    }
// 
//    public List<String> getAvgFields() {
//        return avgFields;
//    }
// 
//    public void setAvgFields(List<String> avgFields) {
//        this.avgFields = avgFields;
//    }
// 
//    public Map<String, SortOrder> getSortField() {
//        return sortField;
//    }
// 
//    public void setSortField(Map<String, SortOrder> sortField) {
//        this.sortField = sortField;
//    }
// 
//    public int getFromIndex() {
//        return fromIndex;
//    }
// 
//    public void setFromIndex(int fromIndex) {
//        this.fromIndex = fromIndex;
//    }
// 
//    public int getSize() {
//        return size;
//    }
// 
//    public void setSize(int size) {
//        this.size = size;
//    }
// 
//    public Map<String, SortOrder> getGroupSortField() {
//        return groupSortField;
//    }
// 
//    public void setGroupSortField(Map<String, SortOrder> groupSortField) {
//        this.groupSortField = groupSortField;
//    }
// 
//    public int getGroupSize() {
//        return groupSize;
//    }
// 
//    public void setGroupSize(int groupSize) {
//        this.groupSize = groupSize;
//    }
//}
