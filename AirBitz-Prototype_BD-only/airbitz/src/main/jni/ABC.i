/*
 * Interface file for swig
 */
%module core

%include "cpointer.i"

/* Create some functions for working with "int *" */
%pointer_functions(int, intp);
%pointer_functions(long, longp);
%pointer_functions(int64_t, int64_tp);
%pointer_functions(double, doublep);

%include typemaps.i
%apply char * { unsigned char * }

%{
#include "ABC_android.h"
%}

/* Let's just grab the original header file here */
%include "ABC_android.h"

%pointer_cast(int64_t *, long *, p64_t_to_long_ptr);
%pointer_cast(int64_t *, double *, p64_t_to_double_ptr);
%pointer_cast(int *, unsigned int *, int_to_uint);
%pointer_cast(long *, tABC_WalletInfo **, longp_to_ppWalletinfo);
%pointer_cast(long *, tABC_WalletInfo *, longp_to_pWalletinfo);
%pointer_cast(long *, tABC_WalletInfo ***, longp_to_pppWalletInfo);
%pointer_cast(long *, tABC_TxInfo ***, longp_to_pppTxInfo);
%pointer_cast(long *, tABC_TxDetails **, longp_to_ppTxDetails);
%pointer_cast(long *, char **, longPtr_to_charPtrPtr);
%pointer_cast(char **, long *, charpp_to_longp);