package com.tangyh.lamp.authority.service.core.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.tangyh.basic.base.service.SuperCacheServiceImpl;
import com.tangyh.basic.cache.model.CacheKeyBuilder;
import com.tangyh.basic.database.mybatis.conditions.Wraps;
import com.tangyh.basic.utils.CollHelper;
import com.tangyh.lamp.authority.dao.core.OrgMapper;
import com.tangyh.lamp.authority.entity.auth.RoleOrg;
import com.tangyh.lamp.authority.entity.core.Org;
import com.tangyh.lamp.authority.service.auth.RoleOrgService;
import com.tangyh.lamp.authority.service.core.OrgService;
import com.tangyh.lamp.common.cache.core.OrgCacheKeyBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 业务实现类
 * 组织
 * </p>
 *
 * @author zuihou
 * @date 2019-07-22
 */
@Slf4j
@Service
@DS("#thread.tenant")
@RequiredArgsConstructor
public class OrgServiceImpl extends SuperCacheServiceImpl<OrgMapper, Org> implements OrgService {
    private final RoleOrgService roleOrgService;

    @Override
    protected CacheKeyBuilder cacheKeyBuilder() {
        return new OrgCacheKeyBuilder();
    }

    @Override
    public List<Org> findChildren(List<Long> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        // MySQL 全文索引
        String applySql = String.format(" MATCH(tree_path) AGAINST('%s' IN BOOLEAN MODE) ", CollUtil.join(ids, " "));

        return super.list(Wraps.<Org>lbQ().in(Org::getId, ids).or(query -> query.apply(applySql)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean remove(List<Long> ids) {
        List<Org> list = this.findChildren(ids);
        List<Long> idList = list.stream().mapToLong(Org::getId).boxed().collect(Collectors.toList());

        boolean bool = idList.isEmpty() || super.removeByIds(idList);

        // 删除自定义类型的数据权限范围
        roleOrgService.remove(Wraps.<RoleOrg>lbQ().in(RoleOrg::getOrgId, idList));
        return bool;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Serializable, Object> findOrgByIds(Set<Serializable> ids) {
        return CollHelper.uniqueIndex(findOrg(ids), Org::getId, org -> org);
    }

    private List<Org> findOrg(Set<Serializable> ids) {
        return findByIds(ids,
                missIds -> super.listByIds(missIds.stream().filter(Objects::nonNull).map(Convert::toLong).collect(Collectors.toList()))
        );
    }

    @Override
    public Map<Serializable, Object> findOrgNameByIds(Set<Serializable> ids) {
        return CollHelper.uniqueIndex(findOrg(ids), Org::getId, Org::getLabel);
    }

}
