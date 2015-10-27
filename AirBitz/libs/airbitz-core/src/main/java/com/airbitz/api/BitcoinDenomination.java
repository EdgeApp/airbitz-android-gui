package com.airbitz.api;

import com.airbitz.api.SWIGTYPE_p_int64_t;

public class BitcoinDenomination {

    tABC_BitcoinDenomination mDenomination;

    protected BitcoinDenomination(tABC_BitcoinDenomination denomination) {
        mDenomination = denomination;
    }

    protected tABC_BitcoinDenomination get() {
        return mDenomination;
    }

    public void setDenominationType(int value) {
        mDenomination.setDenominationType(value);
        if (CoreAPI.ABC_DENOMINATION_MBTC == value) {
            SWIGTYPE_p_int64_t amt = core.new_int64_tp();
            core.longp_assign(core.p64_t_to_long_ptr(amt), 100000);
            mDenomination.setSatoshi(amt);
        } else if (CoreAPI.ABC_DENOMINATION_UBTC == value) {
            SWIGTYPE_p_int64_t amt = core.new_int64_tp();
            core.longp_assign(core.p64_t_to_long_ptr(amt), 100);
            mDenomination.setSatoshi(amt);
        } else if (CoreAPI.ABC_DENOMINATION_BTC == value) {
            SWIGTYPE_p_int64_t amt = core.new_int64_tp();
            core.longp_assign(core.p64_t_to_long_ptr(amt), 100000000);
            mDenomination.setSatoshi(amt);
        }
    }

    public int getDenominationType() {
        return mDenomination.getDenominationType();
    }
}
