package com.campus.repository;

import com.campus.entity.es.CouponDoc;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponDocRepository extends ElasticsearchRepository<CouponDoc, Long> {


    /*
     * 查询所有进行中的优惠券
     * */
    List<CouponDoc> findByStatusOrderByStartTimeDesc(Integer status);


    /*
    * 模糊搜索优惠券
    * */
    @Query("{\"match\": {\"name\": {\"query\": \"?0\"}}}")
    List<CouponDoc> findByName(String name);
}
