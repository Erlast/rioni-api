package com.rioni.lk.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "subaccount_value")
@IdClass(SubaccountValueId.class)
public class SubaccountValue {
    @Id
    @Column(name = "subaccount_id")
    private int subaccountId;

    @Id
    @Column(name = "date")
    private String date;

    @Column(name = "balance_value", columnDefinition = "DECIMAL(19,2)")
    private BigDecimal balanceValue;

    @Column(name = "non_trading_value", columnDefinition = "DECIMAL(19,2)")
    private BigDecimal nonTradingValue;

    @Column(name = "tax_saldo", columnDefinition = "DECIMAL(19,2)")
    private BigDecimal taxSaldo;
}
