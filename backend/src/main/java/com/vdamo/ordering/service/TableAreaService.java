package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.vdamo.ordering.common.exception.BadRequestException;
import com.vdamo.ordering.common.exception.NotFoundException;
import com.vdamo.ordering.common.support.IdGenerator;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.entity.TableAreaEntity;
import com.vdamo.ordering.entity.TableEntity;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.mapper.TableAreaMapper;
import com.vdamo.ordering.mapper.TableMapper;
import com.vdamo.ordering.model.TableAreaEnabledUpdateRequest;
import com.vdamo.ordering.model.TableAreaSummary;
import com.vdamo.ordering.model.TableAreaUpsertRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TableAreaService {

    private final TableAreaMapper tableAreaMapper;
    private final TableMapper tableMapper;
    private final StoreMapper storeMapper;
    private final PermissionService permissionService;
    private final IdGenerator idGenerator;

    public TableAreaService(
            TableAreaMapper tableAreaMapper,
            TableMapper tableMapper,
            StoreMapper storeMapper,
            PermissionService permissionService,
            IdGenerator idGenerator
    ) {
        this.tableAreaMapper = tableAreaMapper;
        this.tableMapper = tableMapper;
        this.storeMapper = storeMapper;
        this.permissionService = permissionService;
        this.idGenerator = idGenerator;
    }

    public List<TableAreaSummary> list(Long storeId, String keyword, Boolean enabled) {
        List<Long> storeScope = permissionService.currentStoreIds();
        if (storeId != null) {
            permissionService.assertStoreAccess(storeId);
        }

        LambdaQueryWrapper<TableAreaEntity> wrapper = new LambdaQueryWrapper<TableAreaEntity>()
                .in(TableAreaEntity::getStoreId, storeScope)
                .orderByAsc(TableAreaEntity::getStoreId)
                .orderByAsc(TableAreaEntity::getSortOrder)
                .orderByAsc(TableAreaEntity::getId);
        if (storeId != null) {
            wrapper.eq(TableAreaEntity::getStoreId, storeId);
        }
        if (enabled != null) {
            wrapper.eq(TableAreaEntity::getEnabled, enabled);
        }
        if (StringUtils.hasText(keyword)) {
            String keywordValue = keyword.trim();
            wrapper.and(query -> query.like(TableAreaEntity::getAreaName, keywordValue)
                    .or()
                    .like(TableAreaEntity::getAreaCode, keywordValue));
        }

        List<TableAreaEntity> areas = tableAreaMapper.selectList(wrapper);
        if (areas.isEmpty()) {
            return List.of();
        }

        Map<Long, String> storeNameMap = storeMapper.selectList(
                        new LambdaQueryWrapper<StoreEntity>().in(StoreEntity::getId, storeScope))
                .stream()
                .collect(Collectors.toMap(StoreEntity::getId, StoreEntity::getName, (left, right) -> left, LinkedHashMap::new));

        List<Long> areaStoreIds = areas.stream()
                .map(TableAreaEntity::getStoreId)
                .distinct()
                .toList();
        Map<Long, Map<String, Long>> tableCountMap = tableMapper.selectList(
                        new LambdaQueryWrapper<TableEntity>().in(TableEntity::getStoreId, areaStoreIds))
                .stream()
                .collect(Collectors.groupingBy(
                        TableEntity::getStoreId,
                        LinkedHashMap::new,
                        Collectors.groupingBy(TableEntity::getAreaName, LinkedHashMap::new, Collectors.counting())));

        return areas.stream()
                .map(area -> toSummary(area, storeNameMap, tableCountMap))
                .toList();
    }

    public TableAreaSummary create(TableAreaUpsertRequest request) {
        permissionService.assertStoreAccess(request.storeId());
        requireStore(request.storeId());

        TableAreaEntity entity = new TableAreaEntity();
        entity.setId(idGenerator.nextId());
        applyAreaValues(entity, request);
        tableAreaMapper.insert(entity);
        return getSummary(entity.getId());
    }

    public TableAreaSummary update(Long id, TableAreaUpsertRequest request) {
        permissionService.assertStoreAccess(request.storeId());
        requireStore(request.storeId());

        TableAreaEntity entity = requireArea(id);
        permissionService.assertStoreAccess(entity.getStoreId());
        String previousAreaName = entity.getAreaName();
        Long previousStoreId = entity.getStoreId();

        applyAreaValues(entity, request);
        tableAreaMapper.updateById(entity);

        if (!previousStoreId.equals(entity.getStoreId()) || !previousAreaName.equals(entity.getAreaName())) {
            tableMapper.update(
                    null,
                    new LambdaUpdateWrapper<TableEntity>()
                            .eq(TableEntity::getStoreId, previousStoreId)
                            .eq(TableEntity::getAreaName, previousAreaName)
                            .set(TableEntity::getStoreId, entity.getStoreId())
                            .set(TableEntity::getAreaName, entity.getAreaName()));
        }
        return getSummary(entity.getId());
    }

    public TableAreaSummary updateEnabled(Long id, TableAreaEnabledUpdateRequest request) {
        TableAreaEntity entity = requireArea(id);
        permissionService.assertStoreAccess(entity.getStoreId());
        entity.setEnabled(request.enabled());
        entity.setUpdater(permissionService.currentUser().username());
        tableAreaMapper.updateById(entity);
        return getSummary(id);
    }

    public void delete(Long id) {
        TableAreaEntity entity = requireArea(id);
        permissionService.assertStoreAccess(entity.getStoreId());
        Long tableCount = tableMapper.selectCount(
                new LambdaQueryWrapper<TableEntity>()
                        .eq(TableEntity::getStoreId, entity.getStoreId())
                        .eq(TableEntity::getAreaName, entity.getAreaName()));
        if (tableCount != null && tableCount > 0) {
            throw new BadRequestException("Table area has tables and cannot be deleted");
        }
        tableAreaMapper.deleteById(entity.getId());
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int resolveCount(Map<Long, Map<String, Long>> countMap, Long storeId, String key) {
        return countMap.getOrDefault(storeId, Map.of())
                .getOrDefault(key, 0L)
                .intValue();
    }

    private void applyAreaValues(TableAreaEntity entity, TableAreaUpsertRequest request) {
        String areaName = request.areaName().trim();
        String areaCode = request.areaCode().trim().toUpperCase(Locale.ROOT);

        validateAreaUnique(entity.getId(), request.storeId(), areaName, areaCode);
        entity.setStoreId(request.storeId());
        entity.setAreaName(areaName);
        entity.setAreaCode(areaCode);
        entity.setSortOrder(request.sortOrder());
        entity.setEnabled(request.enabled());
        entity.setUpdater(permissionService.currentUser().username());
    }

    private void validateAreaUnique(Long currentId, Long storeId, String areaName, String areaCode) {
        List<TableAreaEntity> duplicates = tableAreaMapper.selectList(
                new LambdaQueryWrapper<TableAreaEntity>()
                        .eq(TableAreaEntity::getStoreId, storeId));
        boolean duplicatedName = duplicates.stream()
                .anyMatch(item -> !item.getId().equals(currentId) && item.getAreaName().equalsIgnoreCase(areaName));
        if (duplicatedName) {
            throw new BadRequestException("Area name already exists in this store");
        }
        boolean duplicatedCode = duplicates.stream()
                .anyMatch(item -> !item.getId().equals(currentId) && item.getAreaCode().equalsIgnoreCase(areaCode));
        if (duplicatedCode) {
            throw new BadRequestException("Area code already exists in this store");
        }
    }

    private void requireStore(Long storeId) {
        if (storeMapper.selectById(storeId) == null) {
            throw new NotFoundException("Store not found");
        }
    }

    private TableAreaEntity requireArea(Long id) {
        TableAreaEntity entity = tableAreaMapper.selectById(id);
        if (entity == null) {
            throw new NotFoundException("Table area not found");
        }
        return entity;
    }

    private TableAreaSummary getSummary(Long id) {
        TableAreaEntity entity = requireArea(id);
        Map<Long, String> storeNameMap = storeMapper.selectList(
                        new LambdaQueryWrapper<StoreEntity>().eq(StoreEntity::getId, entity.getStoreId()))
                .stream()
                .collect(Collectors.toMap(StoreEntity::getId, StoreEntity::getName));
        Map<Long, Map<String, Long>> tableCountMap = tableMapper.selectList(
                        new LambdaQueryWrapper<TableEntity>().eq(TableEntity::getStoreId, entity.getStoreId()))
                .stream()
                .collect(Collectors.groupingBy(
                        TableEntity::getStoreId,
                        LinkedHashMap::new,
                        Collectors.groupingBy(TableEntity::getAreaName, LinkedHashMap::new, Collectors.counting())));
        return toSummary(entity, storeNameMap, tableCountMap);
    }

    private TableAreaSummary toSummary(
            TableAreaEntity area,
            Map<Long, String> storeNameMap,
            Map<Long, Map<String, Long>> tableCountMap
    ) {
        return new TableAreaSummary(
                area.getId(),
                area.getStoreId(),
                storeNameMap.getOrDefault(area.getStoreId(), ""),
                area.getAreaName(),
                area.getAreaCode(),
                defaultInt(area.getSortOrder()),
                Boolean.TRUE.equals(area.getEnabled()),
                resolveCount(tableCountMap, area.getStoreId(), area.getAreaName()));
    }
}
