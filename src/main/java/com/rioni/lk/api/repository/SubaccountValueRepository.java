package com.rioni.lk.api.repository;

import com.rioni.lk.api.model.SubaccountValue;
import com.rioni.lk.api.model.SubaccountValueId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SubaccountValueRepository extends JpaRepository<SubaccountValue, SubaccountValueId> {
    @Query("select sv.subaccountId, sv.balanceValue " +
           "from SubaccountValue sv " +
           "where sv.subaccountId in (" +
           "  select s.id from Subaccount s where s.accountId = :accountId" +
           ")")
    List<Object[]> findByAccountId(@Param("accountId") Integer accountId);

    @Query("select coalesce(sum(sv.balanceValue), 0) " +
           "from SubaccountValue sv " +
           "where sv.subaccountId in (" +
           "  select s.id from Subaccount s where s.accountId in (" +
           "    select a.id from Account a where a.profileId = :profileId" +
           "  )" +
           ") " +
           "and sv.date = (" +
           "  select max(sv2.date) from SubaccountValue sv2 where sv2.subaccountId = sv.subaccountId" +
           ")")
    BigDecimal sumLatestBalanceByProfileId(@Param("profileId") Integer profileId);

    @Query("select sv.date, sum(sv.balanceValue) " +
           "from SubaccountValue sv " +
           "where sv.subaccountId in (" +
           "  select s.id from Subaccount s where s.accountId = :accountId" +
           ") " +
           "and sv.date >= :startDate " +
           "group by sv.date " +
           "order by sv.date")
    List<Object[]> findGroupedByDateByAccountId(@Param("accountId") Integer accountId, @Param("startDate") String startDate);
}