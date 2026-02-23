package com.campus.repository;

import com.campus.entity.es.SetmealDoc;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SetmealDocRepository extends ElasticsearchRepository<SetmealDoc,Long> {


    /*
    * 模糊匹配
    * */
    @Query("{\"bool\":{\"must\":[{\"multi_match\":{\"query\":\"?0\",\"fields\":[\"name\",\"description\"]}}],\"filter\":[{\"term\":{\"status\":?1}}]}}")
    List<SetmealDoc> searchByKeyword(String keyWord, Integer enable);
}
