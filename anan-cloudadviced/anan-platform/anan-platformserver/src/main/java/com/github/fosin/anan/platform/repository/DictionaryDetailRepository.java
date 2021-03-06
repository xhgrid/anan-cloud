package com.github.fosin.anan.platform.repository;

import com.github.fosin.anan.platformapi.entity.AnanDictionaryDetailEntity;
import com.github.fosin.anan.jpa.repository.IJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Lazy;

import java.util.List;

/**
 * 2017/12/27.
 * Time:16:09
 *
 * @author fosin
 */
@Repository
@Lazy
public interface DictionaryDetailRepository extends IJpaRepository<AnanDictionaryDetailEntity, Long> {
    List<AnanDictionaryDetailEntity> findByDictionaryId(Long dictionaryId);

    void deleteAllByDictionaryId(Long dictionaryId);
}
