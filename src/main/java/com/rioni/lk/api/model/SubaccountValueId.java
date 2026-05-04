package com.rioni.lk.api.model;

import java.io.Serializable;
import java.util.Objects;

public class SubaccountValueId implements Serializable {
    private int subaccountId;
    private String date;

    public SubaccountValueId() {}

    public SubaccountValueId(int subaccountId, String date) {
        this.subaccountId = subaccountId;
        this.date = date;
    }

    public int getSubaccountId() { return subaccountId; }
    public void setSubaccountId(int subaccountId) { this.subaccountId = subaccountId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubaccountValueId that = (SubaccountValueId) o;
        return subaccountId == that.subaccountId && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subaccountId, date);
    }
}