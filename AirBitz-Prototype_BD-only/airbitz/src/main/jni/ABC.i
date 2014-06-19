/*
 * Interface file for swig
 */
%module core

%include "cpointer.i"

/* Create some functions for working with "int *" */
%pointer_functions(int, intp);
%pointer_cast(int *, unsigned int *, int_to_uint);
%pointer_functions(long, longp);

%include typemaps.i
%apply char * { unsigned char * }

%{
#include "ABC_android.h"
%}

/* Let's just grab the original header file here */
%include "ABC_android.h"

%pointer_cast(long *, tABC_WalletInfo ***, longPtr_to_walletinfoPtr);
