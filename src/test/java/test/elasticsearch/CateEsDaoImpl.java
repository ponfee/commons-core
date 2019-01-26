//package test.elasticsearch;
//
//public class CateEsDaoImpl extends EsDbUtils implements CateEsDao {  
//    @Override  
//    public List<CateVenderData> queryListByFilterQuery(EsQueryObj esQueryObj) throws Exception {  
//        return (List<CateVenderData>)queryObjectListByFilterQuery(esQueryObj,CateVenderData.class);  
//    }  
//   
//    @Override  
//    public List<CateVenderData> queryListByFilterQueryWithAgg(EsQueryObj esQueryObj) throws Exception {  
//        return (List<CateVenderData>)queryObjectListByFilterQueryWithAgg(esQueryObj,CateVenderData.class);  
//    }  
//}  
//   
//   
//public class RealTimeEsDaoImpl extends EsDbUtils implements RealTimeEsDao  {  
//    @Override  
//    public List<OdpOperatorSum> queryListByFilterQuery(EsQueryObj esQueryObj) throws Exception {  
//        return (List<OdpOperatorSum>)queryObjectListByFilterQuery(esQueryObj,OdpOperatorSum.class);  
//    }  
//   
//    @Override  
//    public List<OdpOperatorSum> queryListByFilterQueryWithAgg(EsQueryObj esQueryObj) throws Exception {  
//        return (List<OdpOperatorSum>)queryObjectListByFilterQueryWithAgg(esQueryObj,OdpOperatorSum.class);  
//    }  
//}  