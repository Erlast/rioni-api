package com.rioni.lk.api.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

import static com.rioni.lk.api.model.QAsset.asset;
import static com.rioni.lk.api.model.QQuote.quote;
import static com.rioni.lk.api.model.QSubaccount.subaccount;
import static com.rioni.lk.api.model.QSubaccountAsset.subaccountAsset;
import static com.rioni.lk.api.model.QAccount.account;

@Repository
public class SubaccountAssetRepositoryImpl implements SubaccountAssetRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public SubaccountAssetRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * Builds the SELECT expressions matching the original JPQL:
     *   saa,
     *   cast(round(saa.purchasePrice * saa.amount, 2) as BigDecimal) as investedValue,
     *   (subquery last quote)  as balanceValue,
     *   (subquery bid quote)   as bid,
     *   (subquery ask quote)   as ask
     */
    private Expression<?>[] buildSelectExpressions() {
        return new Expression<?>[] {
            subaccountAsset,
            Expressions.numberTemplate(BigDecimal.class,
                "cast(round({0} * {1}, 2) as BigDecimal)",
                subaccountAsset.purchasePrice, subaccountAsset.amount),
            // correlated subquery: last quote -> balanceValue
            JPAExpressions
                .select(quote.quoteValue)
                .from(quote)
                .where(quote.assetId.eq(asset.assetId)
                    .and(quote.quoteTypeCode.eq("last")))
                .orderBy(quote.date.desc())
                .limit(1),
            // correlated subquery: bid quote
            JPAExpressions
                .select(quote.quoteValue)
                .from(quote)
                .where(quote.assetId.eq(asset.assetId)
                    .and(quote.quoteTypeCode.eq("bid")))
                .orderBy(quote.date.desc())
                .limit(1),
            // correlated subquery: ask quote
            JPAExpressions
                .select(quote.quoteValue)
                .from(quote)
                .where(quote.assetId.eq(asset.assetId)
                    .and(quote.quoteTypeCode.eq("ask")))
                .orderBy(quote.date.desc())
                .limit(1)
        };
    }

    @Override
    public List<Tuple> findAssetsByProfileId(Integer profileId,
                                             String assetTypeCode,
                                             String search) {
        return baseSelectQuery()
            .join(subaccountAsset.subaccount, subaccount)
            .join(subaccount.account, account)
            .where(account.profileId.eq(profileId))
            .where(buildSearchPredicate(assetTypeCode, search))
            .orderBy(asset.assetName.asc())
            .fetch();
    }

    @Override
    public long countAssetsByProfileId(Integer profileId,
                                       String assetTypeCode,
                                       String search) {
        return baseCountQuery()
            .join(subaccountAsset.subaccount, subaccount)
            .join(subaccount.account, account)
            .where(account.profileId.eq(profileId))
            .where(buildSearchPredicate(assetTypeCode, search))
            .fetchOne();
    }

    @Override
    public List<Tuple> findAssetsByAccountId(Integer accountId,
                                             String assetTypeCode,
                                             String search) {
        return baseSelectQuery()
            .join(subaccountAsset.subaccount, subaccount)
            .where(subaccount.accountId.eq(accountId))
            .where(buildSearchPredicate(assetTypeCode, search))
            .orderBy(asset.assetName.asc())
            .fetch();
    }

    @Override
    public long countAssetsByAccountId(Integer accountId,
                                       String assetTypeCode,
                                       String search) {
        return baseCountQuery()
            .join(subaccountAsset.subaccount, subaccount)
            .where(subaccount.accountId.eq(accountId))
            .where(buildSearchPredicate(assetTypeCode, search))
            .fetchOne();
    }

    // ----  base query builders  ----

    private JPAQuery<Tuple> baseSelectQuery() {
        return queryFactory
            .select(buildSelectExpressions())
            .from(subaccountAsset)
            .join(subaccountAsset.asset, asset).fetchJoin();
    }

    private JPAQuery<Long> baseCountQuery() {
        return queryFactory
            .select(subaccountAsset.count())
            .from(subaccountAsset)
            .join(subaccountAsset.asset, asset);
    }

    // ----  filter predicates  ----

    private BooleanExpression buildSearchPredicate(String assetTypeCode, String search) {
        BooleanExpression predicate = null;

        if (assetTypeCode != null && !assetTypeCode.isEmpty()) {
            predicate = and(predicate, asset.assetTypeCode.eq(assetTypeCode));
        }

        if (search != null && !search.isEmpty()) {
            BooleanExpression searchExpr = asset.assetName.containsIgnoreCase(search)
                .or(asset.baseTicker.containsIgnoreCase(search));
            predicate = and(predicate, searchExpr);
        }

        return predicate;
    }

    private static BooleanExpression and(BooleanExpression left, BooleanExpression right) {
        return left != null ? left.and(right) : right;
    }
}
