package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.entity.MemberEntity;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.mapper.MemberMapper;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.model.MemberSummary;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MemberService {

    private final MemberMapper memberMapper;
    private final StoreMapper storeMapper;
    private final PermissionService permissionService;

    public MemberService(
            MemberMapper memberMapper,
            StoreMapper storeMapper,
            PermissionService permissionService
    ) {
        this.memberMapper = memberMapper;
        this.storeMapper = storeMapper;
        this.permissionService = permissionService;
    }

    public List<MemberSummary> list(Long storeId, String countryCode, String levelCode, String keyword) {
        List<Long> storeScope = permissionService.currentStoreIds();
        if (storeId != null) {
            permissionService.assertStoreAccess(storeId);
        }

        LambdaQueryWrapper<MemberEntity> wrapper = new LambdaQueryWrapper<MemberEntity>()
                .in(MemberEntity::getStoreId, storeScope)
                .orderByAsc(MemberEntity::getStoreId)
                .orderByDesc(MemberEntity::getCreateTime);
        if (storeId != null) {
            wrapper.eq(MemberEntity::getStoreId, storeId);
        }
        if (StringUtils.hasText(levelCode)) {
            wrapper.eq(MemberEntity::getLevelCode, levelCode.trim());
        }
        if (StringUtils.hasText(countryCode)) {
            wrapper.eq(MemberEntity::getCountryCode, countryCode.trim());
        }
        if (StringUtils.hasText(keyword)) {
            String keywordValue = keyword.trim();
            wrapper.and(q -> q.like(MemberEntity::getDisplayName, keywordValue)
                    .or()
                    .like(MemberEntity::getPhoneNational, keywordValue)
                    .or()
                    .like(MemberEntity::getPhoneE164, keywordValue));
        }

        Map<Long, String> storeNameMap = storeMapper.selectList(
                        new LambdaQueryWrapper<StoreEntity>().in(StoreEntity::getId, storeScope))
                .stream()
                .collect(Collectors.toMap(StoreEntity::getId, StoreEntity::getName, (left, right) -> left));

        return memberMapper.selectList(wrapper)
                .stream()
                .map(entity -> toSummary(entity, storeNameMap))
                .toList();
    }

    public MemberSummary findByPhone(String phoneE164) {
        List<Long> storeScope = permissionService.currentStoreIds();
        MemberEntity entity = memberMapper.selectOne(
                new LambdaQueryWrapper<MemberEntity>()
                        .eq(MemberEntity::getPhoneE164, phoneE164)
                        .in(MemberEntity::getStoreId, storeScope)
        );
        if (entity == null) {
            return null;
        }

        Map<Long, String> storeNameMap = storeMapper.selectList(
                        new LambdaQueryWrapper<StoreEntity>().in(StoreEntity::getId, storeScope))
                .stream()
                .collect(Collectors.toMap(StoreEntity::getId, StoreEntity::getName, (left, right) -> left));
        return toSummary(entity, storeNameMap);
    }

    private MemberSummary toSummary(MemberEntity entity, Map<Long, String> storeNameMap) {
        return new MemberSummary(
                entity.getId(),
                entity.getStoreId(),
                storeNameMap.getOrDefault(entity.getStoreId(), ""),
                entity.getLevelCode(),
                entity.getDisplayName(),
                entity.getCountryCode(),
                entity.getPhoneNational(),
                entity.getPhoneE164()
        );
    }
}
