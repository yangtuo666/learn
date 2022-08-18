package com.zhang.jiu.learn.elasticSearch;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

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
        user.setId("10001");
        user.setAge(10);
        user.setName("小明");
        Teacher teacher = new Teacher();
        teacher.setTeacherAge(40);
        teacher.setTeacherName("老王");
        user.setTeacher(teacher);
        //elasticsearchUtil.createIndex(User.class);
        elasticsearchUtil.singleAdd(user);

        String userjson = elasticsearchUtil.queryById("user_index", user.getId());
        System.out.println(userjson);

    }

}
