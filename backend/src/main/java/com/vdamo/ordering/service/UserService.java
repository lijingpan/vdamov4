package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.common.exception.BadRequestException;
import com.vdamo.ordering.common.exception.NotFoundException;
import com.vdamo.ordering.common.support.IdGenerator;
import com.vdamo.ordering.entity.StoreEntity;
import com.vdamo.ordering.entity.SysRoleEntity;
import com.vdamo.ordering.entity.SysUserEntity;
import com.vdamo.ordering.entity.SysUserRoleEntity;
import com.vdamo.ordering.entity.SysUserStoreEntity;
import com.vdamo.ordering.mapper.StoreMapper;
import com.vdamo.ordering.mapper.SysRoleMapper;
import com.vdamo.ordering.mapper.SysUserMapper;
import com.vdamo.ordering.mapper.SysUserRoleMapper;
import com.vdamo.ordering.mapper.SysUserStoreMapper;
import com.vdamo.ordering.model.UserSummary;
import com.vdamo.ordering.model.UserUpsertRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysUserStoreMapper sysUserStoreMapper;
    private final SysRoleMapper sysRoleMapper;
    private final StoreMapper storeMapper;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final IdGenerator idGenerator;

    public UserService(
            SysUserMapper sysUserMapper,
            SysUserRoleMapper sysUserRoleMapper,
            SysUserStoreMapper sysUserStoreMapper,
            SysRoleMapper sysRoleMapper,
            StoreMapper storeMapper,
            RoleService roleService,
            PermissionService permissionService,
            IdGenerator idGenerator
    ) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysUserStoreMapper = sysUserStoreMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.storeMapper = storeMapper;
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.idGenerator = idGenerator;
    }

    public List<UserSummary> listAll(String keyword, Boolean enabled, Long storeId) {
        LambdaQueryWrapper<SysUserEntity> wrapper = new LambdaQueryWrapper<SysUserEntity>()
                .orderByAsc(SysUserEntity::getId);
        if (enabled != null) {
            wrapper.eq(SysUserEntity::getEnabled, enabled);
        }
        if (StringUtils.hasText(keyword)) {
            String keywordValue = keyword.trim();
            wrapper.and(query -> query.like(SysUserEntity::getUsername, keywordValue)
                    .or()
                    .like(SysUserEntity::getDisplayName, keywordValue));
        }

        List<UserSummary> summaries = sysUserMapper.selectList(wrapper).stream()
                .map(this::toSummary)
                .toList();
        if (storeId == null) {
            return summaries;
        }
        return summaries.stream()
                .filter(summary -> summary.storeIds().contains(storeId))
                .toList();
    }

    public UserSummary create(UserUpsertRequest request) {
        if (!StringUtils.hasText(request.password())) {
            throw new BadRequestException("Password is required");
        }

        SysUserEntity entity = new SysUserEntity();
        entity.setId(idGenerator.nextId());
        applyUserValues(entity, request);
        entity.setPassword(request.password().trim());
        sysUserMapper.insert(entity);
        syncUserRoles(entity.getId(), request.roleIds());
        syncUserStores(entity.getId(), request.storeIds());
        return toSummary(entity);
    }

    public UserSummary update(Long id, UserUpsertRequest request) {
        SysUserEntity entity = requireUser(id);
        applyUserValues(entity, request);
        if (StringUtils.hasText(request.password())) {
            entity.setPassword(request.password().trim());
        }
        sysUserMapper.updateById(entity);
        syncUserRoles(id, request.roleIds());
        syncUserStores(id, request.storeIds());
        return toSummary(entity);
    }

    public SysUserEntity findByUsername(String username) {
        return sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getUsername, username)
        );
    }

    public List<Long> listRoleIds(Long userId) {
        return sysUserRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getUserId, userId))
                .stream()
                .map(SysUserRoleEntity::getRoleId)
                .toList();
    }

    public List<Long> listStoreIds(Long userId) {
        return sysUserStoreMapper.selectList(
                        new LambdaQueryWrapper<SysUserStoreEntity>().eq(SysUserStoreEntity::getUserId, userId))
                .stream()
                .map(SysUserStoreEntity::getStoreId)
                .toList();
    }

    public UserSummary toSummary(SysUserEntity entity) {
        List<SysRoleEntity> roles = roleService.listEntitiesByUserId(entity.getId());
        List<Long> roleIds = roles.stream()
                .map(SysRoleEntity::getId)
                .toList();
        List<String> roleCodes = roles.stream()
                .map(SysRoleEntity::getCode)
                .toList();
        List<Long> storeIds = listStoreIds(entity.getId());
        Map<Long, String> storeNameById = storeIds.isEmpty()
                ? Map.of()
                : storeMapper.selectBatchIds(storeIds).stream()
                        .filter(store -> store != null)
                        .collect(Collectors.toMap(StoreEntity::getId, StoreEntity::getName, (left, right) -> left));
        List<String> storeNames = storeIds.isEmpty()
                ? List.of()
                : storeIds.stream()
                        .map(id -> storeNameById.getOrDefault(id, ""))
                        .filter(StringUtils::hasText)
                        .toList();
        return new UserSummary(
                entity.getId(),
                entity.getUsername(),
                entity.getDisplayName(),
                Boolean.TRUE.equals(entity.getEnabled()),
                roleIds,
                roleCodes,
                storeIds,
                storeNames
        );
    }

    private void applyUserValues(SysUserEntity entity, UserUpsertRequest request) {
        String username = request.username().trim();
        String displayName = request.displayName().trim();
        List<Long> roleIds = normalizeIds(request.roleIds());
        List<Long> storeIds = normalizeIds(request.storeIds());

        validateUsernameUnique(entity.getId(), username);
        validateRoleIds(roleIds);
        validateStoreIds(storeIds);

        entity.setUsername(username);
        entity.setDisplayName(displayName);
        entity.setEnabled(Boolean.TRUE.equals(request.enabled()));
        entity.setUpdater(permissionService.currentUser().username());
    }

    private void validateUsernameUnique(Long currentId, String username) {
        List<SysUserEntity> duplicates = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getUsername, username));
        boolean exists = duplicates.stream().anyMatch(item -> !item.getId().equals(currentId));
        if (exists) {
            throw new BadRequestException("Username already exists");
        }
    }

    private void validateRoleIds(List<Long> roleIds) {
        if (roleIds.isEmpty()) {
            throw new BadRequestException("At least one role is required");
        }
        long existingCount = sysRoleMapper.selectBatchIds(roleIds).stream()
                .filter(item -> item != null)
                .count();
        if (existingCount != roleIds.size()) {
            throw new BadRequestException("Some roles do not exist");
        }
    }

    private void validateStoreIds(List<Long> storeIds) {
        if (storeIds.isEmpty()) {
            throw new BadRequestException("At least one store is required");
        }
        long existingCount = storeMapper.selectBatchIds(storeIds).stream()
                .filter(item -> item != null)
                .count();
        if (existingCount != storeIds.size()) {
            throw new BadRequestException("Some stores do not exist");
        }
    }

    private void syncUserRoles(Long userId, List<Long> roleIds) {
        List<Long> normalizedRoleIds = normalizeIds(roleIds);
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getUserId, userId));
        String username = permissionService.currentUser().username();
        normalizedRoleIds.forEach(roleId -> {
            SysUserRoleEntity entity = new SysUserRoleEntity();
            entity.setId(idGenerator.nextId());
            entity.setUserId(userId);
            entity.setRoleId(roleId);
            entity.setUpdater(username);
            sysUserRoleMapper.insert(entity);
        });
    }

    private void syncUserStores(Long userId, List<Long> storeIds) {
        List<Long> normalizedStoreIds = normalizeIds(storeIds);
        sysUserStoreMapper.delete(new LambdaQueryWrapper<SysUserStoreEntity>().eq(SysUserStoreEntity::getUserId, userId));
        String username = permissionService.currentUser().username();
        normalizedStoreIds.forEach(storeId -> {
            SysUserStoreEntity entity = new SysUserStoreEntity();
            entity.setId(idGenerator.nextId());
            entity.setUserId(userId);
            entity.setStoreId(storeId);
            entity.setUpdater(username);
            sysUserStoreMapper.insert(entity);
        });
    }

    private SysUserEntity requireUser(Long id) {
        SysUserEntity entity = sysUserMapper.selectById(id);
        if (entity == null) {
            throw new NotFoundException("User not found");
        }
        return entity;
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream().filter(item -> item != null).distinct().toList();
    }
}
