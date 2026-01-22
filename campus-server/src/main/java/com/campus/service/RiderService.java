package com.campus.service;

import com.campus.dto.RiderDTO;
import com.campus.dto.RiderLoginDTO;
import com.campus.vo.RiderLoginVO;

public interface RiderService {
    void register(RiderLoginDTO riderLoginDTO);

    RiderLoginVO login(RiderLoginDTO riderLoginDTO);

    void updateById(RiderDTO riderDTO);
}
