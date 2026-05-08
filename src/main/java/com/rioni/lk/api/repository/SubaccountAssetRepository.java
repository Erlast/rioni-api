package com.rioni.lk.api.repository;

import com.rioni.lk.api.model.SubaccountAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface SubaccountAssetRepository extends JpaRepository<SubaccountAsset, Integer> {
    @Query("select saa, " +
            "cast(round(saa.purchasePrice * saa.amount, 2) as BigDecimal) as investedValue, " +
           "(select q.quoteValue from Quote q where q.assetId = ast.assetId and q.quoteTypeCode = 'last' order by q.date desc fetch first 1 rows only) as balanceValue, " +
            "(select q.quoteValue from Quote q where q.assetId = ast.assetId and q.quoteTypeCode = 'bid' order by q.date desc fetch first 1 rows only) as bid, " +
            "(select q.quoteValue from Quote q where q.assetId = ast.assetId and q.quoteTypeCode = 'ask' order by q.date desc fetch first 1 rows only) as ask " +
            "from SubaccountAsset saa " +
            "join fetch saa.asset ast " +
            "join saa.subaccount s " +
            "join s.account a " +
            "where a.profileId = :profileId " +
            "and (:assetTypeCode is null or ast.assetTypeCode = :assetTypeCode)")
    List<Object[]> findByProfileId(@Param("profileId") Integer profileId, @Param("assetTypeCode") String assetTypeCode);
}
