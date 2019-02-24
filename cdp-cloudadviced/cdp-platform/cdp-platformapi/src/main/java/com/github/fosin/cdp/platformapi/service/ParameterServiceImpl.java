package com.github.fosin.cdp.platformapi.service;


import com.github.fosin.cdp.jpa.repository.IJpaRepository;
import com.github.fosin.cdp.platformapi.dto.request.CdpParameterCreateDto;
import com.github.fosin.cdp.platformapi.dto.request.CdpParameterUpdateDto;
import com.github.fosin.cdp.platformapi.repository.ParameterRepository;
import com.github.fosin.cdp.core.exception.CdpServiceException;
import com.github.fosin.cdp.mvc.module.PageModule;
import com.github.fosin.cdp.mvc.result.Result;
import com.github.fosin.cdp.mvc.result.ResultUtils;
import com.github.fosin.cdp.platformapi.constant.TableNameConstant;
import com.github.fosin.cdp.platformapi.entity.CdpParameterEntity;
import com.github.fosin.cdp.platformapi.entity.CdpUserEntity;
import com.github.fosin.cdp.platformapi.service.inter.IParameterService;
import com.github.fosin.cdp.platformapi.util.LoginUserUtil;
import com.github.fosin.cdp.cache.util.CacheUtil;
import com.github.fosin.cdp.util.ClassUtil;
import com.github.fosin.cdp.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.criteria.*;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 字典表服务
 *
 * @author fosin
 * @date 2018-7-29
 */
@Service
@Lazy
public class ParameterServiceImpl implements IParameterService {
    @Autowired
    private ParameterRepository parameterRepository;

    @Override
    @CachePut(value = TableNameConstant.CDP_PARAMETER, key = "#root.target.getCacheKey(#result)")
    public CdpParameterEntity create(CdpParameterCreateDto entity) {
        Assert.notNull(entity, "传入的创建数据实体对象不能为空!");
        CdpParameterEntity createEntiy = new CdpParameterEntity();
        BeanUtils.copyProperties(entity, createEntiy);
        return getRepository().save(createEntiy);
    }

    @Override
    public CdpParameterEntity update(CdpParameterUpdateDto entity) {
        Assert.notNull(entity, "传入了空对象!");
        Long id = entity.getId();
        Assert.notNull(id, "ID不能为空!");
        CdpParameterEntity cEntity = parameterRepository.findById(id).get();
        BeanUtils.copyProperties(entity, cEntity);
        CdpParameterEntity save = parameterRepository.save(cEntity);
        String cCacheKey = getCacheKey(cEntity);
        String cacheKey = getCacheKey(entity.getType(), entity.getScope(), entity.getName());
        //如果修改了type、scope、name则需要删除以前的缓存并设置新缓存
        if (!cCacheKey.equals(cacheKey)) {
            //新key设置旧值，需要发布以后才刷新缓存换成本次更新的新值
            CacheUtil.put(TableNameConstant.CDP_PARAMETER, cacheKey, cEntity);
            CacheUtil.evict(TableNameConstant.CDP_PARAMETER, cCacheKey);
        }
        return save;
    }

    @Override
    public CdpParameterEntity deleteById(Long id) {
        CdpParameterEntity entity = parameterRepository.findById(id).get();
        Assert.notNull(entity, "通过ID没有能找到参数数据,删除被取消!");
        String cacheKey = getCacheKey(entity);
        CacheUtil.evict(TableNameConstant.CDP_PARAMETER, cacheKey);
        parameterRepository.deleteById(id);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new CdpServiceException(e);
        }
        CacheUtil.evict(TableNameConstant.CDP_PARAMETER, cacheKey);
        return null;
    }

    @Override
    @CacheEvict(value = TableNameConstant.CDP_PARAMETER, key = "#root.target.getCacheKey(#entity)")
    public CdpParameterEntity deleteByEntity(CdpParameterEntity entity) {
        Assert.notNull(entity, "传入了空对象!");
        parameterRepository.delete(entity);
        return entity;
    }

    @Override
    public Result findAllByPageSort(PageModule pageModule) {
        PageRequest pageable = PageRequest.of(pageModule.getPageNumber() - 1, pageModule.getPageSize(), Sort.Direction.fromString(pageModule.getSortOrder()), pageModule.getSortName());
        String searchCondition = pageModule.getSearchText();

        Specification<CdpParameterEntity> condition = (Specification<CdpParameterEntity>) (root, query, cb) -> {
            Path<String> name = root.get("name");
            Path<String> scope = root.get("scope");
            Path<String> value = root.get("value");
            if (StringUtils.isBlank(searchCondition)) {
                return query.getRestriction();
            }
            return cb.or(cb.like(scope, "%" + searchCondition + "%"), cb.like(name, "%" + searchCondition + "%"), cb.like(value, "%" + searchCondition + "%"));

        };
        //分页查找
        Page<CdpParameterEntity> page = parameterRepository.findAll(condition, pageable);

        return ResultUtils.success(page.getTotalElements(), page.getContent());
    }

    public String getCacheKey(CdpParameterEntity entity) {
        return getCacheKey(entity.getType(), entity.getScope(), entity.getName());
    }

    public String getCacheKey(Integer type, String scope, String name) {
        if (StringUtil.isEmpty(scope)) {
            scope = "";
        }
        return type + "-" + scope + "-" + name;
    }

    @Override
    @Cacheable(value = TableNameConstant.CDP_PARAMETER, key = "#root.target.getCacheKey(#type,#scope,#name)")
    public CdpParameterEntity findByTypeAndScopeAndName(Integer type, String scope, String name) {
        CdpParameterEntity entity = parameterRepository.findByTypeAndScopeAndName(type, scope, name);
        //因为参数会逐级上上级机构查找，为减少没有必要的查询，该代码为解决Spring Cache默认不缓存null值问题
        if (entity == null) {
            entity = new CdpParameterEntity();
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = CdpServiceException.class)
    public boolean applyChange(Long id) {
        CdpParameterEntity entity = parameterRepository.findById(id).get();
        Assert.notNull(entity, "该参数已经不存在!");
        String cacheKey = getCacheKey(entity);
        CdpUserEntity loginUser;
        boolean success;
        switch (entity.getStatus()) {
            case 1:
                loginUser = LoginUserUtil.getUser();
                entity.setApplyBy(loginUser.getId());
                entity.setApplyTime(new Date());
                entity.setStatus(0);
                success = CacheUtil.put(TableNameConstant.CDP_PARAMETER, cacheKey, entity);
                if (success) {
                    parameterRepository.save(entity);
                }
                break;
            case 2:
                success = CacheUtil.evict(TableNameConstant.CDP_PARAMETER, cacheKey);
                if (success) {
                    parameterRepository.deleteById(id);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new CdpServiceException(e);
                    }
                    success = CacheUtil.evict(TableNameConstant.CDP_PARAMETER, cacheKey);
                }
                break;
            default:
                success = CacheUtil.put(TableNameConstant.CDP_PARAMETER, cacheKey, entity);
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = CdpServiceException.class)
    public boolean applyChanges() {
        List<CdpParameterEntity> entities = parameterRepository.findByStatusNot(0);
        Assert.isTrue(entities != null && entities.size() != 0, "没有更改过任何参数，不需要发布!");
        for (CdpParameterEntity entity : entities) {
            applyChange(entity.getId());
        }
        return true;
    }

    @Override
    public IJpaRepository<CdpParameterEntity, Long> getRepository() {
        return parameterRepository;
    }

//    protected synchronized void addCacheEvict(CdpParameterEntity entity) {
//        Set<CdpParameterEntity> set = CacheUtil.get(CDP_PARAMETER_EVICTCACHE, CDP_PARAMETER_EVICTCACHE, Set.class);
//        if (set == null) {
//            set = new HashSet<>();
//        }
//        set.add(entity);
//        CacheUtil.put(CDP_PARAMETER_EVICTCACHE, CDP_PARAMETER_EVICTCACHE, set);
//    }
//
//    protected void removeCacheEvict(CdpParameterEntity entity) {
//        Set<CdpParameterEntity> set = CacheUtil.get(CDP_PARAMETER_EVICTCACHE, CDP_PARAMETER_EVICTCACHE, Set.class);
//        if (set == null || set.size() == 0) {
//            return;
//        }
//        for (CdpParameterEntity e : set) {
//            if (e.getId().equals(entity.getId())) {
//                set.remove(e);
//            }
//        }
//        CacheUtil.put(CDP_PARAMETER_EVICTCACHE, CDP_PARAMETER_EVICTCACHE, set);
//    }
}
