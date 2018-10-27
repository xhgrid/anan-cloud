package com.github.fosin.cdp.platformapi.service.inter;

import com.github.fosin.cdp.platformapi.entity.CdpSysParameterEntity;
import com.github.fosin.cdp.core.exception.CdpServiceException;
import com.github.fosin.cdp.mvc.service.ISimpleService;

/**
 *  2017/12/29.
 * Time:12:30
 * @author fosin
 */
public interface IParameterService extends ISimpleService<CdpSysParameterEntity,Long> {
    CdpSysParameterEntity findByTypeAndScopeAndName(Integer type, String scope, String name);

    boolean applyChange(Long id) throws CdpServiceException;

    boolean applyChanges() throws CdpServiceException;
}