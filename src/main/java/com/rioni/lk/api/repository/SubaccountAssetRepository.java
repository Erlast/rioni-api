package com.rioni.lk.api.repository;

import com.rioni.lk.api.model.SubaccountAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubaccountAssetRepository extends JpaRepository<SubaccountAsset, Integer>,
        SubaccountAssetRepositoryCustom {

}
