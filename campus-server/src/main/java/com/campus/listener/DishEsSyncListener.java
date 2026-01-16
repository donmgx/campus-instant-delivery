package com.campus.listener;

import com.campus.entity.Dish;
import com.campus.entity.es.DishDoc;
import com.campus.event.DishChangedEvent;
import com.campus.mapper.DishMapper;
import com.campus.repository.DishDocRepository;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/*
 * 菜品变更监听器
 * */
@Slf4j
@Component
public class DishEsSyncListener {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private RBloomFilter<Long> dishBloomFilter;

    @Autowired
    private DishDocRepository dishDocRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void syncDishToEs(DishChangedEvent event) {
        log.info("监听到菜品变更事件,开始同步ES... ids：{}, type：{}", event.getDishIds(), event.getType());
        //查询数据库
        if (event.getType().equals(DishChangedEvent.OPERATE_DELETE)) {
            //删除事件
            dishDocRepository.deleteAllById(event.getDishIds());
            log.info("ES数据删除完成");
        } else {
            //如果是更新或插入事件
            List<DishDoc> dishDocs = new ArrayList<>();
            for (Long dishId : event.getDishIds()) {
                //查询更新后的数据库菜品数据
                Dish dish = dishMapper.getById(dishId);
                if (dish != null) {

                    DishDoc dishDoc = new DishDoc();
                    BeanUtils.copyProperties(dish, dishDoc);

                    dishDocs.add(dishDoc);
                    //同步到布隆过滤器
                    dishBloomFilter.add(dishId);
                }
            }

            if (!CollectionUtils.isEmpty(dishDocs)) {
                //用不到es
                dishDocRepository.saveAll(dishDocs);
                log.info("ES同步完成，共同步{}条", dishDocs.size());
            }
        }
    }
}
