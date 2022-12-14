package com.zhang.jiu.learn.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.zhang.jiu.learn.Enum.FieldTypeEnum;
import com.zhang.jiu.learn.annotation.EsField;
import com.zhang.jiu.learn.annotation.EsId;
import com.zhang.jiu.learn.annotation.IndexName;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @className: EsUtil
 * @description: es ???????????????;
 *      ????????????????????????????????????
 * @author: sh.Liu
 * @create: 2020-05-25 09:41
 */
@Component
@Slf4j
public class ElasticsearchUtil {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * ????????????(??????????????????5???????????????1)
     * @param clazz ????????????????????????es??????
     * @throws IOException
     */
    public boolean createIndex(Class clazz) throws Exception {
        IndexName declaredAnnotation = (IndexName)clazz.getDeclaredAnnotation(IndexName.class);
        if(declaredAnnotation == null){
            throw new Exception(String.format("class name: %s can not find Annotation [Document], please check", clazz.getName()));
        }
        String indexName = declaredAnnotation.index();
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        request.settings(Settings.builder()
                // ??????????????????3??? ?????????2
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2)
        );
        request.mapping(generateBuilder(clazz));
        CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        // ??????????????????????????????????????????
        boolean acknowledged = response.isAcknowledged();
        // ???????????????????????????????????????????????????????????????????????????????????????
        boolean shardsAcknowledged = response.isShardsAcknowledged();
        if (acknowledged || shardsAcknowledged) {
            log.info("????????????????????????????????????{}", indexName);
            return true;
        }
        return false;
    }



    /**
     * ????????????(??????????????????5???????????????1)???
     * ????????????????????????????????????????????????
     * ??????????????????????????????
     *
     * @param clazz ????????????????????????es??????
     * @throws IOException
     */
    public boolean updateIndex(Class clazz) throws Exception {
        IndexName declaredAnnotation = (IndexName)clazz.getDeclaredAnnotation(IndexName.class);
        if(declaredAnnotation == null){
            throw new Exception(String.format("class name: %s can not find Annotation [Document], please check", clazz.getName()));
        }
        String indexName = declaredAnnotation.index();
        PutMappingRequest request = new PutMappingRequest(indexName);

        request.source(generateBuilder(clazz));
        AcknowledgedResponse response = restHighLevelClient.indices().putMapping(request, RequestOptions.DEFAULT);
        // ??????????????????????????????????????????
        boolean acknowledged = response.isAcknowledged();

        if (acknowledged ) {
            log.info("??????????????????????????????????????????{}", indexName);
            return true;
        }
        return false;
    }
    /**
     * ????????????
     * @param indexName
     * @return
     */
    public boolean delIndex(String indexName){
        boolean acknowledged = false;
        try {
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
            deleteIndexRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
            AcknowledgedResponse delete = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            acknowledged = delete.isAcknowledged();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return acknowledged;
    }

    /**
     * ????????????????????????
     * @param indexName
     * @return
     */
    public boolean isIndexExists(String indexName){
        boolean exists = false;
        try {
            GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
            getIndexRequest.humanReadable(true);
            exists = restHighLevelClient.indices().exists(getIndexRequest,RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exists;
    }


    /**
     * ??????????????????
     * ?????????????????????
     *  1. json
     *  2. map
     *      Map<String, Object> jsonMap = new HashMap<>();
     *      jsonMap.put("user", "kimchy");
     *      jsonMap.put("postDate", new Date());
     *      jsonMap.put("message", "trying out Elasticsearch");
     *      IndexRequest indexRequest = new IndexRequest("posts")
     *          .id("1").source(jsonMap);
     *  3. builder
     *      XContentBuilder builder = XContentFactory.jsonBuilder();
     *      builder.startObject();
     *      {
     *          builder.field("user", "kimchy");
     *          builder.timeField("postDate", new Date());
     *          builder.field("message", "trying out Elasticsearch");
     *      }
     *      builder.endObject();
     *      IndexRequest indexRequest = new IndexRequest("posts")
     *      .id("1").source(builder);
     * 4. source:
     *      IndexRequest indexRequest = new IndexRequest("posts")
     *     .id("1")
     *     .source("user", "kimchy",
     *         "postDate", new Date(),
     *         "message", "trying out Elasticsearch");
     *
     *   ?????????  Validation Failed: 1: type is missing;
     *      ????????????jar?????????
     *
     *   ??????????????????????????????
     *
     * @return
     */
    public IndexResponse singleAdd(Object o) throws Exception {
        IndexName declaredAnnotation = (IndexName )o.getClass().getDeclaredAnnotation(IndexName.class);
        if(declaredAnnotation == null){
            throw new Exception(String.format("class name: %s can not find Annotation [Document], please check", o.getClass().getName()));
        }
        String indexName = declaredAnnotation.index();

        IndexRequest request = new IndexRequest(indexName);
        Field fieldByAnnotation = getFieldByAnnotation(o, EsId.class);
        if (fieldByAnnotation != null) {
            fieldByAnnotation.setAccessible(true);
            try {
                Object id = fieldByAnnotation.get(o);
                request =request.id(id.toString());
            } catch (IllegalAccessException e) {
                log.error("??????id???????????????{}", e);
            }
        }

        String json = JSON.toJSONString(o);
        request.source(json, XContentType.JSON);
        IndexResponse indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        return indexResponse;
    }


    /**
     * ??????id??????
     * @return
     */
    public String queryById(String indexName, String id) throws IOException {
        GetRequest getRequest = new GetRequest(indexName, id);
        // getRequest.fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE);

        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        String jsonStr = getResponse.getSourceAsString();
        return jsonStr;
    }

    /**
     * ??????????????????json?????????
     * @param indexName
     * @param searchSourceBuilder
     * @return
     * @throws IOException
     */
    public String search(String indexName, SearchSourceBuilder searchSourceBuilder) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(searchSourceBuilder);
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHits hits = searchResponse.getHits();
        JSONArray jsonArray = new JSONArray();
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            JSONObject jsonObject = JSON.parseObject(sourceAsString);
            jsonArray.add(jsonObject);
        }
        log.info("??????????????????" + hits.getTotalHits());
        return jsonArray.toJSONString();
    }

    /**
     * ????????????????????????
     * @param searchSourceBuilder
     * @param pageNum
     * @param pageSize
     * @param s
     * @param <T>
     * @return
     * @throws IOException
     */
    public <T> PageInfo<T> search(SearchSourceBuilder searchSourceBuilder, int pageNum, int pageSize, Class<T> s) throws Exception {
        IndexName declaredAnnotation = (IndexName )s.getDeclaredAnnotation(IndexName.class);
        if(declaredAnnotation == null){
            throw new Exception(String.format("class name: %s can not find Annotation [Document], please check", s.getName()));
        }
        String indexName = declaredAnnotation.index();
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        JSONArray jsonArray = new JSONArray();
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            JSONObject jsonObject = JSON.parseObject(sourceAsString);
            jsonArray.add(jsonObject);
        }
        log.info("??????????????????" + hits.getTotalHits());
        int total = (int)hits.getTotalHits().value;

        // ????????????
        List<T> list = jsonArray.toJavaList(s);
        PageInfo<T> page = new PageInfo<>();
        page.setList(list);
        page.setPageNum(pageNum);
        page.setPageSize(pageSize);
        page.setTotal(total);
        page.setPages(total== 0 ? 0: (total%pageSize == 0 ? total / pageSize : (total / pageSize) + 1));
        page.setHasNextPage(page.getPageNum() < page.getPages());
        return page;
    }

    /**
     * ???????????????????????????
     * @param searchSourceBuilder
     * @param s
     * @param <T>
     * @return
     * @throws IOException
     */
    public <T> List<T> search(SearchSourceBuilder searchSourceBuilder, Class<T> s) throws Exception {
        IndexName declaredAnnotation = s.getDeclaredAnnotation(IndexName.class);
        if(declaredAnnotation == null){
            throw new Exception(String.format("class name: %s can not find Annotation [Document], please check", s.getName()));
        }
        String indexName = declaredAnnotation.index();
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(searchSourceBuilder);
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHits hits = searchResponse.getHits();
        JSONArray jsonArray = new JSONArray();
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            JSONObject jsonObject = JSON.parseObject(sourceAsString);
            jsonArray.add(jsonObject);
        }
        // ????????????
        List<T> list = jsonArray.toJavaList(s);
        return list;
    }


    /**
     * ??????????????????
     * ???????????? ?????????
     * ??????????????? ?????????
     * @param list
     * @return
     */
    public <T> boolean batchSaveOrUpdate(List<T> list) throws Exception {
        Object o1 = list.get(0);
        IndexName declaredAnnotation = o1.getClass().getDeclaredAnnotation(IndexName.class);
        if(declaredAnnotation == null){
            throw new Exception(String.format("class name: %s can not find Annotation [@Document], please check", o1.getClass().getName()));
        }
        String indexName = declaredAnnotation.index();

        BulkRequest request = new BulkRequest(indexName);
        for (Object o : list) {
            String jsonStr = JSON.toJSONString(o);
            IndexRequest indexReq = new IndexRequest().source(jsonStr, XContentType.JSON);

            Field fieldByAnnotation = getFieldByAnnotation(o, EsId.class);
            if (fieldByAnnotation != null) {
                fieldByAnnotation.setAccessible(true);
                try {
                    Object id = fieldByAnnotation.get(o);
                    indexReq = indexReq.id(id.toString());
                } catch (IllegalAccessException e) {
                    log.error("??????id???????????????{}", e);
                }
            }
            request.add(indexReq);
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);

        for(BulkItemResponse bulkItemResponse : bulkResponse){
            DocWriteResponse itemResponse = bulkItemResponse.getResponse();
            IndexResponse indexResponse = (IndexResponse) itemResponse;
            log.info("?????????????????????{}", indexResponse);
            if(bulkItemResponse.isFailed()){
                log.error("es ????????????{}",bulkItemResponse.getFailureMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * ????????????
     * @param indexName??? ????????????
     * @param docId???     ??????id
     */
    public boolean deleteDoc(String indexName, String docId) throws IOException {
        DeleteRequest request = new DeleteRequest(indexName, docId);
        DeleteResponse deleteResponse = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        // ??????response
        String index = deleteResponse.getIndex();
        String id = deleteResponse.getId();
        long version = deleteResponse.getVersion();
        ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();
        if (shardInfo.getFailed() > 0) {
            for (ReplicationResponse.ShardInfo.Failure failure :
                    shardInfo.getFailures()) {
                String reason = failure.reason();
                log.info("???????????????????????? {}", reason);
            }
        }
        return true;
    }

    /**
     * ??????json??????????????????
     * @param indexName
     * @param docId
     * @param o
     * @return
     * @throws IOException
     */
    public boolean updateDoc(String indexName, String docId, Object o) throws IOException {
        UpdateRequest request = new UpdateRequest(indexName, docId);
        request.doc(JSON.toJSONString(o), XContentType.JSON);
        UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        String index = updateResponse.getIndex();
        String id = updateResponse.getId();
        long version = updateResponse.getVersion();
        if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
            return true;
        } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            return true;
        } else if (updateResponse.getResult() == DocWriteResponse.Result.DELETED) {
        } else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {

        }
        return false;
    }

    /**
     * ??????Map??????????????????
     * @param indexName
     * @param docId
     * @param map
     * @return
     * @throws IOException
     */
    public boolean updateDoc(String indexName, String docId, Map<String, Object> map) throws IOException {
        UpdateRequest request = new UpdateRequest(indexName, docId);
        request.doc(map);
        UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        String index = updateResponse.getIndex();
        String id = updateResponse.getId();
        long version = updateResponse.getVersion();
        if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
            return true;
        } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            return true;
        } else if (updateResponse.getResult() == DocWriteResponse.Result.DELETED) {
        } else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {

        }
        return false;
    }


    public XContentBuilder generateBuilder(Class clazz) throws IOException {
        // ???????????????????????????
        IndexName doc = (IndexName) clazz.getAnnotation(IndexName.class);
        System.out.println(doc.index());

        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        builder.startObject("properties");
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field f : declaredFields) {
            if (f.isAnnotationPresent(EsField.class)) {
                // ????????????
                EsField  declaredAnnotation = f.getDeclaredAnnotation(EsField.class);
                if (declaredAnnotation.type() == FieldTypeEnum.OBJECT) {
                    // ????????????????????????-- Action
                    Class<?> type = f.getType();
                    Field[] df2 = type.getDeclaredFields();
                    builder.startObject(f.getName());
                    builder.startObject("properties");
                    // ?????????????????????????????????
                    for (Field f2 : df2) {
                        if (f2.isAnnotationPresent(EsField.class)) {
                            // ????????????
                            EsField declaredAnnotation2 = f2.getDeclaredAnnotation(EsField.class);
                            builder.startObject(f2.getName());
                            builder.field("type", declaredAnnotation2.type().getType());
                            // keyword???????????????
                            if (declaredAnnotation2.type() == FieldTypeEnum.TEXT) {
                                builder.field("analyzer", declaredAnnotation2.analyzer().getType());
                            }
                            if (declaredAnnotation2.keywordAttach()){
                                builder.startObject("fields");
                                builder.startObject("keyword");
                                builder.field("type","keyword");
                                builder.field("ignore_above",256);
                                builder.endObject();
                                builder.endObject();
                            }
                            if (declaredAnnotation2.type() == FieldTypeEnum.DATE) {
                                builder.field("format", "yyyy-MM-dd HH:mm:ss");
                            }
                            builder.endObject();
                        }
                    }
                    builder.endObject();
                    builder.endObject();

                }else{
                    builder.startObject(f.getName());
                    builder.field("type", declaredAnnotation.type().getType());
                    // keyword???????????????
                    if (declaredAnnotation.type() == FieldTypeEnum.TEXT) {
                        builder.field("analyzer", declaredAnnotation.analyzer().getType());
                    }
                    if (declaredAnnotation.keywordAttach()){
                        builder.startObject("fields");
                        builder.startObject("keyword");
                        builder.field("type","keyword");
                        builder.field("ignore_above",256);
                        builder.endObject();
                        builder.endObject();
                    }
                    if (declaredAnnotation.type() == FieldTypeEnum.DATE) {
                        builder.field("format", "yyyy-MM-dd HH:mm:ss");
                    }
                    builder.endObject();
                }
            }
        }
        // ??????property
        builder.endObject();
        builder.endObject();
        return builder;
    }


    public static Field getFieldByAnnotation(Object o ,Class annotationClass){
        Field[] declaredFields = o.getClass().getDeclaredFields();
        if (declaredFields != null && declaredFields.length >0) {
            for(Field f : declaredFields){
                if (f.isAnnotationPresent(annotationClass)) {
                    return f;
                }
            }
        }
        return null;
    }

}

