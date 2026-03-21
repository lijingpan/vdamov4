package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.common.exception.BadRequestException;
import com.vdamo.ordering.common.exception.NotFoundException;
import com.vdamo.ordering.common.support.IdGenerator;
import com.vdamo.ordering.entity.MemberEntity;
import com.vdamo.ordering.entity.OrderEntity;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.mapper.MemberMapper;
import com.vdamo.ordering.mapper.OrderMapper;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.model.MemberSummary;
import com.vdamo.ordering.model.MemberUpsertRequest;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MemberService {

    private final MemberMapper memberMapper;
    private final OrderMapper orderMapper;
    private final StoreMapper storeMapper;
    private final PermissionService permissionService;
    private final IdGenerator idGenerator;

    public MemberService(
            MemberMapper memberMapper,
            OrderMapper orderMapper,
            StoreMapper storeMapper,
            PermissionService permissionService,
            IdGenerator idGenerator
    ) {
        this.memberMapper = memberMapper;
        this.orderMapper = orderMapper;
        this.storeMapper = storeMapper;
        this.permissionService = permissionService;
        this.idGenerator = idGenerator;
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
            wrapper.eq(MemberEntity::getLevelCode, levelCode.trim().toUpperCase(Locale.ROOT));
        }
        if (StringUtils.hasText(countryCode)) {
            wrapper.eq(MemberEntity::getCountryCode, countryCode.trim().toUpperCase(Locale.ROOT));
        }
        if (StringUtils.hasText(keyword)) {
            String keywordValue = keyword.trim();
            wrapper.and(q -> q.like(MemberEntity::getDisplayName, keywordValue)
                    .or()
                    .like(MemberEntity::getPhoneNational, keywordValue)
                    .or()
                    .like(MemberEntity::getPhoneE164, keywordValue));
        }

        Map<Long, String> storeNameMap = loadStoreNameMap(storeScope);
        return memberMapper.selectList(wrapper)
                .stream()
                .map(entity -> toSummary(entity, storeNameMap))
                .toList();
    }

    public MemberSummary create(MemberUpsertRequest request) {
        permissionService.assertStoreAccess(request.storeId());
        requireStore(request.storeId());

        MemberEntity entity = new MemberEntity();
        entity.setId(idGenerator.nextId());
        applyMemberValues(entity, request);
        memberMapper.insert(entity);
        return getSummary(entity.getId());
    }

    public MemberSummary update(Long id, MemberUpsertRequest request) {
        permissionService.assertStoreAccess(request.storeId());
        requireStore(request.storeId());

        MemberEntity entity = requireMember(id);
        permissionService.assertStoreAccess(entity.getStoreId());
        applyMemberValues(entity, request);
        memberMapper.updateById(entity);
        return getSummary(id);
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

        Map<Long, String> storeNameMap = loadStoreNameMap(storeScope);
        return toSummary(entity, storeNameMap);
    }

    public void delete(Long id) {
        MemberEntity entity = requireMember(id);
        permissionService.assertStoreAccess(entity.getStoreId());

        Long orderCount = orderMapper.selectCount(
                new LambdaQueryWrapper<OrderEntity>().eq(OrderEntity::getMemberId, id));
        if (orderCount != null && orderCount > 0) {
            throw new BadRequestException("Member is referenced by orders and cannot be deleted");
        }

        memberMapper.deleteById(entity.getId());
    }

    private void applyMemberValues(MemberEntity entity, MemberUpsertRequest request) {
        String levelCode = request.levelCode().trim().toUpperCase(Locale.ROOT);
        String displayName = request.displayName().trim();
        String countryCode = request.countryCode().trim().toUpperCase(Locale.ROOT);
        String phoneNational = request.phoneNational().trim();
        String phoneE164 = request.phoneE164().trim();

        validateMemberPhoneUnique(entity.getId(), phoneE164);

        entity.setStoreId(request.storeId());
        entity.setLevelCode(levelCode);
        entity.setDisplayName(displayName);
        entity.setCountryCode(countryCode);
        entity.setPhoneNational(phoneNational);
        entity.setPhoneE164(phoneE164);
        entity.setUpdater(permissionService.currentUser().username());
    }

    private void validateMemberPhoneUnique(Long currentId, String phoneE164) {
        List<MemberEntity> duplicates = memberMapper.selectList(
                new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getPhoneE164, phoneE164));
        boolean exists = duplicates.stream().anyMatch(item -> !item.getId().equals(currentId));
        if (exists) {
            throw new BadRequestException("Member phone already exists");
        }
    }

    private void requireStore(Long storeId) {
        if (storeMapper.selectById(storeId) == null) {
            throw new NotFoundException("Store not found");
        }
    }

    private MemberEntity requireMember(Long id) {
        MemberEntity entity = memberMapper.selectById(id);
        if (entity == null) {
            throw new NotFoundException("Member not found");
        }
        return entity;
    }

    private MemberSummary getSummary(Long id) {
        MemberEntity entity = requireMember(id);
        Map<Long, String> storeNameMap = loadStoreNameMap(List.of(entity.getStoreId()));
        return toSummary(entity, storeNameMap);
    }

    private Map<Long, String> loadStoreNameMap(List<Long> storeIds) {
        return storeMapper.selectList(
                        new LambdaQueryWrapper<StoreEntity>().in(StoreEntity::getId, storeIds))
                .stream()
                .collect(Collectors.toMap(StoreEntity::getId, StoreEntity::getName, (left, right) -> left));
    }

    private MemberSummary toSummary(MemberEntity entity, Map<Long, String> storeNameMap) {
        return new MemberSummary(
                entity.getId(),
                entity.getStoreId(),
                storeNameMap.getOrDefault(entity.getStoreId(), ""),
                normalizeOptional(entity.getLevelCode()),
                normalizeOptional(entity.getDisplayName()),
                normalizeOptional(entity.getCountryCode()),
                normalizeOptional(entity.getPhoneNational()),
                normalizeOptional(entity.getPhoneE164())
        );
    }

    private String normalizeOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim();
    }
}
