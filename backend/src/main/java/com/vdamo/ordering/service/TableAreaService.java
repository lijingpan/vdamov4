package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.entity.TableAreaEntity;
import com.vdamo.ordering.entity.TableEntity;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.mapper.TableAreaMapper;
import com.vdamo.ordering.mapper.TableMapper;
import com.vdamo.ordering.model.TableAreaSummary;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TableAreaService {

    private final TableAreaMapper tableAreaMapper;
    private final TableMapper tableMapper;
    private final StoreMapper storeMapper;
    private final PermissionService permissionService;

    public TableAreaService(
            TableAreaMapper tableAreaMapper,
            TableMapper tableMapper,
            StoreMapper storeMapper,
            PermissionService permissionService
    ) {
        this.tableAreaMapper = tableAreaMapper;
        this.tableMapper = tableMapper;
        this.storeMapper = storeMapper;
        this.permissionService = permissionService;
    }

    public List<TableAreaSummary> list(Long storeId, String keyword) {
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
                .map(area -> new TableAreaSummary(
                        area.getId(),
                        area.getStoreId(),
                        storeNameMap.getOrDefault(area.getStoreId(), ""),
                        area.getAreaName(),
                        area.getAreaCode(),
                        defaultInt(area.getSortOrder()),
                        Boolean.TRUE.equals(area.getEnabled()),
                        resolveCount(tableCountMap, area.getStoreId(), area.getAreaName())))
                .toList();
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int resolveCount(Map<Long, Map<String, Long>> countMap, Long storeId, String key) {
        return countMap.getOrDefault(storeId, Map.of())
                .getOrDefault(key, 0L)
                .intValue();
    }
}
