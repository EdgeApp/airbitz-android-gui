/*
 * Interface file for swig
 */
%module core


// 3:
%typemap(jstype) callback_r cb "CallbackRequestResults";
%typemap(jtype) callback_r cb "CallbackRequestResults";
%typemap(jni) callback_r cb "jobject";
%typemap(javain) callback_r cb "$javainput";


%include typemaps.i
%apply char * { unsigned char * }

%{
#include "ABC_android.h"
%}

/* Let's just grab the original header file here */
%include "ABC_android.h"

