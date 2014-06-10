/*
 * Interface file for swig
 */
%module core

%include typemaps.i
%apply char * { unsigned char * }

%{
#include "ABC_android.h"
%}

/* Let's just grab the original header file here */
%include "ABC_android.h"

