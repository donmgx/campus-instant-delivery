package com.campus.listener;

import com.campus.entity.Setmeal;
import com.campus.entity.es.SetmealDoc;
import com.campus.event.SetmealChangedEvent;
import com.campus.mapper.SetmealMapper;
import com.campus.repository.SetmealDocRepository;
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
 * 套餐变更监听器
 * */
@Slf4j
@Component
public class SetmealEsSyncListener {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private RBloomFilter setmealBloomFilter;

    @Autowired
    private SetmealDocRepository setmealDocRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void syncSetmealToEs(SetmealChangedEvent event) {
        log.info("监听到套餐变更事件,开始同步ES... ids：{}, type：{}", event.getSetmealIds(), event.getType());
        //判断事件类型
        if (event.getType().equals(SetmealChangedEvent.OPERATE_DELETE)) {
            //如果是删除事件
            setmealDocRepository.deleteAllById(event.getSetmealIds());
            log.info("ES删除完成");
        } else {
            //如果是更新或插入事件
            List<SetmealDoc> setmealDocs = new ArrayList<>();
            for (Long setmealId : event.getSetmealIds()) {
                Setmeal setmeal = setmealMapper.selectById(setmealId);
                if (setmeal != null) {
                    SetmealDoc setmealDoc = new SetmealDoc();
                    BeanUtils.copyProperties(setmeal, setmealDoc);
                    setmealDocs.add(setmealDoc);
                    //同步添加到布隆过滤器
                    setmealBloomFilter.add(setmealId);
                }
            }

            //同步到ES
            if (!CollectionUtils.isEmpty(setmealDocs)) {
                setmealDocRepository.saveAll(setmealDocs);
                log.info("ES同步完成,同步条数为：{}", setmealDocs.size());
            }
        }

    }
}
