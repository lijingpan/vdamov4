package com.vdamo.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vdamo.ordering.entity.MemberEntity;
import com.vdamo.ordering.mapper.MemberMapper;
import com.vdamo.ordering.model.MemberSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    private final MemberMapper memberMapper;
    private final PermissionService permissionService;

    public MemberService(MemberMapper memberMapper, PermissionService permissionService) {
        this.memberMapper = memberMapper;
        this.permissionService = permissionService;
    }

    public List<MemberSummary> list() {
        return memberMapper.selectList(
                        new LambdaQueryWrapper<MemberEntity>()
                                .in(MemberEntity::getStoreId, permissionService.currentStoreIds()))
                .stream()
                .map(this::toSummary)
                .toList();
    }

    public MemberSummary findByPhone(String phoneE164) {
        MemberEntity entity = memberMapper.selectOne(
                new LambdaQueryWrapper<MemberEntity>()
                        .eq(MemberEntity::getPhoneE164, phoneE164)
                        .in(MemberEntity::getStoreId, permissionService.currentStoreIds())
        );
        if (entity == null) {
            return null;
        }
        return toSummary(entity);
    }

    private MemberSummary toSummary(MemberEntity entity) {
        return new MemberSummary(
                entity.getId(),
                entity.getLevelCode(),
                entity.getDisplayName(),
                entity.getCountryCode(),
                entity.getPhoneNational(),
                entity.getPhoneE164()
        );
    }
}
