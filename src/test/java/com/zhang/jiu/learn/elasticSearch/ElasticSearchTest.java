package com.zhang.jiu.learn.elasticSearch;

import com.github.pagehelper.PageInfo;
import com.zhang.jiu.learn.po.Teacher;
import com.zhang.jiu.learn.po.User;
import com.zhang.jiu.learn.utils.ElasticsearchUtil;
import org.apache.http.HttpHost;


import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class ElasticSearchTest {
    @Autowired
    ElasticsearchUtil elasticsearchUtil;

    /**
     * 创建索引测试
     */
    @Test
    void createIndex() throws Exception {
        User user = new User();
        user.setId("10002");
        user.setAge(16);
        user.setName("小明ddd");
        Teacher teacher = new Teacher();
        teacher.setTeacherAge(45);
        teacher.setTeacherName("更改蛋");
        user.setTeacher(teacher);
        //elasticsearchUtil.createIndex(User.class);
        //新增文档
        //elasticsearchUtil.singleAdd(user);
        //更新文档
        //elasticsearchUtil.updateDoc("user_index",user.getId(),user);

        //根据id查询
        //String userjson = elasticsearchUtil.queryById("user_index", user.getId());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //精确查询(要查询的字段必须是keyword)
        //TermQueryBuilder queryBuilder = QueryBuilders.termQuery("name.keyword", "小明");
        //MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("name.keyword", "小明");
        //前缀查询
        //PrefixQueryBuilder queryBuilder = QueryBuilders.prefixQuery("name", "小明");
        //模糊查询
        //MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("name", "小明");
        //范围查询
        //RangeQueryBuilder queryBuilder = QueryBuilders.rangeQuery("age").from(12).to(16).includeLower(true);
        //and 查询
        //BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("name", "小明")).must(QueryBuilders.rangeQuery("age").from(15).includeLower(true));
        //or 查询
        //BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("name.keyword", "小明")).should(QueryBuilders.rangeQuery("age").from(15).includeLower(true));
        //二级属性字段查询
        //MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("teacher.teacherName", "更改");
        List<String> names = new ArrayList<>();
        names.add("小明");
        //in 查询
        //TermsQueryBuilder queryBuilder = QueryBuilders.termsQuery("name.keyword", names);
        //not in 查询
        //BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().mustNot(QueryBuilders.termsQuery("name.keyword", names));
        //通配符检索
        WildcardQueryBuilder queryBuilder = QueryBuilders.wildcardQuery("teacher.teacherName.keyword", "更?蛋");
        searchSourceBuilder.query(queryBuilder);
        PageInfo<User> search = elasticsearchUtil.search(searchSourceBuilder, 1, 10, User.class);


        System.out.println(search);

    }

}
