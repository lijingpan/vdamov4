package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.common.exception.BadRequestException;
import com.vdamo.ordering.common.exception.NotFoundException;
import com.vdamo.ordering.common.support.IdGenerator;
import com.vdamo.ordering.entity.DiscountEntity;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.mapper.DiscountMapper;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.model.DiscountEnabledUpdateRequest;
import com.vdamo.ordering.model.DiscountSummary;
import com.vdamo.ordering.model.DiscountUpsertRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DiscountService {

    private static final Set<String> DISCOUNT_TYPES = Set.of(
            "MEMBER_PRICE",
            "ORDER_DISCOUNT",
            "FULL_REDUCTION",
            "COUPON"
    );
    private static final Set<String> AMOUNT_TYPES = Set.of("FIXED", "PERCENT");

    private final DiscountMapper discountMapper;
    private final StoreMapper storeMapper;
    private final PermissionService permissionService;
    private final IdGenerator idGenerator;

    public DiscountService(
            DiscountMapper discountMapper,
            StoreMapper storeMapper,
            PermissionService permissionService,
            IdGenerator idGenerator
    ) {
        this.discountMapper = discountMapper;
        this.storeMapper = storeMapper;
        this.permissionService = permissionService;
        this.idGenerator = idGenerator;
    }

    public List<DiscountSummary> list(Long storeId, Boolean enabled, String discountType, String keyword) {
        List<Long> storeScope = permissionService.currentStoreIds();
        if (storeId != null) {
            permissionService.assertStoreAccess(storeId);
        }
        String normalizedDiscountType = StringUtils.hasText(discountType) ? normalizeDiscountType(discountType) : null;

        LambdaQueryWrapper<DiscountEntity> wrapper = new LambdaQueryWrapper<DiscountEntity>()
                .in(DiscountEntity::getStoreId, storeScope)
                .orderByAsc(DiscountEntity::getStoreId)
                .orderByAsc(DiscountEntity::getDiscountType)
                .orderByAsc(DiscountEntity::getName)
                .orderByAsc(DiscountEntity::getId);
        if (storeId != null) {
            wrapper.eq(DiscountEntity::getStoreId, storeId);
        }
        if (enabled != null) {
            wrapper.eq(DiscountEntity::getEnabled, enabled);
        }
        if (normalizedDiscountType != null) {
            wrapper.eq(DiscountEntity::getDiscountType, normalizedDiscountType);
        }
        if (StringUtils.hasText(keyword)) {
            String keywordValue = keyword.trim();
            wrapper.and(query -> query.like(DiscountEntity::getName, keywordValue)
                    .or()
                    .like(DiscountEntity::getCode, keywordValue)
                    .or()
                    .like(DiscountEntity::getRemark, keywordValue));
        }

        List<DiscountEntity> discounts = discountMapper.selectList(wrapper);
        if (discounts.isEmpty()) {
            return List.of();
        }

        Map<Long, String> storeNameMap = loadStoreNameMap(storeScope);
        return discounts.stream()
                .map(discount -> toSummary(discount, storeNameMap))
                .toList();
    }

    public DiscountSummary create(DiscountUpsertRequest request) {
        permissionService.assertStoreAccess(request.storeId());
        requireStore(request.storeId());

        DiscountEntity entity = new DiscountEntity();
        entity.setId(idGenerator.nextId());
        applyDiscountValues(entity, request);
        discountMapper.insert(entity);
        return getSummary(entity.getId());
    }

    public DiscountSummary update(Long id, DiscountUpsertRequest request) {
        permissionService.assertStoreAccess(request.storeId());
        requireStore(request.storeId());

        DiscountEntity entity = requireDiscount(id);
        permissionService.assertStoreAccess(entity.getStoreId());
        applyDiscountValues(entity, request);
        discountMapper.updateById(entity);
        return getSummary(id);
    }

    public DiscountSummary updateEnabled(Long id, DiscountEnabledUpdateRequest request) {
        DiscountEntity entity = requireDiscount(id);
        permissionService.assertStoreAccess(entity.getStoreId());
        entity.setEnabled(request.enabled());
        entity.setUpdater(permissionService.currentUser().username());
        discountMapper.updateById(entity);
        return getSummary(id);
    }

    public void delete(Long id) {
        DiscountEntity entity = requireDiscount(id);
        permissionService.assertStoreAccess(entity.getStoreId());
        discountMapper.deleteById(entity.getId());
    }

    private void applyDiscountValues(DiscountEntity entity, DiscountUpsertRequest request) {
        String name = request.name().trim();
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        String discountType = normalizeDiscountType(request.discountType());
        String amountType = normalizeAmountType(request.amountType());
        Integer amountValue = request.amountValue();
        Integer thresholdAmountInCent = request.thresholdAmountInCent();
        LocalDateTime startTime = request.startTime();
        LocalDateTime endTime = request.endTime();

        validateCodeUnique(entity.getId(), request.storeId(), code);
        validateAmountValue(amountType, amountValue);
        validateTimeRange(startTime, endTime);

        entity.setStoreId(request.storeId());
        entity.setName(name);
        entity.setCode(code);
        entity.setDiscountType(discountType);
        entity.setAmountType(amountType);
        entity.setAmountValue(amountValue);
        entity.setThresholdAmountInCent(thresholdAmountInCent);
        entity.setStackable(Boolean.TRUE.equals(request.stackable()));
        entity.setEnabled(Boolean.TRUE.equals(request.enabled()));
        entity.setStartTime(startTime);
        entity.setEndTime(endTime);
        entity.setRemark(normalizeOptional(request.remark()));
        entity.setUpdater(permissionService.currentUser().username());
    }

    private void validateCodeUnique(Long currentId, Long storeId, String code) {
        List<DiscountEntity> duplicates = discountMapper.selectList(
                new LambdaQueryWrapper<DiscountEntity>().eq(DiscountEntity::getStoreId, storeId));
        boolean exists = duplicates.stream()
                .anyMatch(item -> !item.getId().equals(currentId) && item.getCode() != null && item.getCode().equalsIgnoreCase(code));
        if (exists) {
            throw new BadRequestException("Discount code already exists in this store");
        }
    }

    private void validateAmountValue(String amountType, Integer amountValue) {
        if (amountValue == null || amountValue <= 0) {
            throw new BadRequestException("Amount value must be greater than 0");
        }
        if ("PERCENT".equals(amountType) && amountValue > 100) {
            throw new BadRequestException("Percent discount value must be less than or equal to 100");
        }
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BadRequestException("Start time must be earlier than end time");
        }
    }

    private String normalizeDiscountType(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!DISCOUNT_TYPES.contains(normalized)) {
            throw new BadRequestException("Unsupported discount type");
        }
        return normalized;
    }

    private String normalizeAmountType(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!AMOUNT_TYPES.contains(normalized)) {
            throw new BadRequestException("Unsupported amount type");
        }
        return normalized;
    }

    private void requireStore(Long storeId) {
        if (storeMapper.selectById(storeId) == null) {
            throw new NotFoundException("Store not found");
        }
    }

    private DiscountEntity requireDiscount(Long id) {
        DiscountEntity entity = discountMapper.selectById(id);
        if (entity == null) {
            throw new NotFoundException("Discount not found");
        }
        return entity;
    }

    private DiscountSummary getSummary(Long id) {
        DiscountEntity entity = requireDiscount(id);
        Map<Long, String> storeNameMap = loadStoreNameMap(List.of(entity.getStoreId()));
        return toSummary(entity, storeNameMap);
    }

    private Map<Long, String> loadStoreNameMap(List<Long> storeIds) {
        return storeMapper.selectList(new LambdaQueryWrapper<StoreEntity>().in(StoreEntity::getId, storeIds))
                .stream()
                .collect(Collectors.toMap(StoreEntity::getId, StoreEntity::getName, (left, right) -> left, LinkedHashMap::new));
    }

    private DiscountSummary toSummary(DiscountEntity entity, Map<Long, String> storeNameMap) {
        return new DiscountSummary(
                entity.getId(),
                entity.getStoreId(),
                storeNameMap.getOrDefault(entity.getStoreId(), ""),
                normalizeOptional(entity.getName()),
                normalizeOptional(entity.getCode()),
                normalizeOptional(entity.getDiscountType()),
                normalizeOptional(entity.getAmountType()),
                defaultInt(entity.getAmountValue()),
                defaultInt(entity.getThresholdAmountInCent()),
                Boolean.TRUE.equals(entity.getStackable()),
                Boolean.TRUE.equals(entity.getEnabled()),
                entity.getStartTime(),
                entity.getEndTime(),
                normalizeOptional(entity.getRemark()));
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String normalizeOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim();
    }
}
