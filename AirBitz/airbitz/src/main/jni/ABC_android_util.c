#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include <ABC.h>
#include <android/log.h>

/*
 * Asynchronous callback for Transactions
*/
// cached refs for later callbacks
JavaVM * g_vm;
jobject g_obj;
jmethodID g_mid_callback;
void *bitcoinInfo;

void bitcoinCallback(const tABC_AsyncBitCoinInfo *pInfo) {
	JNIEnv * g_env;
	// double check it's all ok
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "Entering bitcoinCallback");
	int getEnvStat = (*g_vm)->GetEnv(g_vm, (void **)&g_env, JNI_VERSION_1_6);
	if (getEnvStat == JNI_EDETACHED) {
//        __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "GetEnv: not attached");
		if ((*g_vm)->AttachCurrentThread(g_vm, (struct JNINativeInterface const ***) &g_env, NULL) != 0) {
//            __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "Failed to attach");
		}
	} else if (getEnvStat == JNI_OK) {
	} else if (getEnvStat == JNI_EVERSION) {
//        __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "GetEnv: version not supported");
	}

//        __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "bitcoinCallback calling Java");
    (*g_env)->CallVoidMethod(g_env, g_obj, g_mid_callback, (void *) pInfo);
//        __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "bitcoinCallback java Called");

//
//	if ((*g_env)->ExceptionCheck(g_env)) {
//		(*g_env)->ExceptionDescribe(g_env);
//	}

//	(*g_vm)->DetachCurrentThread(g_vm);
}

void ABC_BitCoin_Event_Callback(const tABC_AsyncBitCoinInfo *pInfo)
{
    bitcoinCallback(pInfo);
}

JNIEXPORT jint JNICALL
Java_com_airbitz_api_CoreAPI_coreDataSyncAccount(JNIEnv *jenv, jclass jcls, jstring jusername, jstring jpassword, jlong jerrorp) {
  jint jresult = 0 ;
  char *username = (char *) 0 ;
  char *password = (char *) 0 ;
  tABC_BitCoin_Event_Callback callback = (tABC_BitCoin_Event_Callback) 0 ;
  void *bcInfo = (void *) 0 ;
  tABC_Error *errorp = (tABC_Error *) 0 ;
  tABC_CC result;

  (void)jenv;
  (void)jcls;

  username = 0;
  if (jusername) {
    username = (char *)(*jenv)->GetStringUTFChars(jenv, jusername, 0);
    if (!username) return 0;
  }
  password = 0;
  if (jpassword) {
    password = (char *)(*jenv)->GetStringUTFChars(jenv, jpassword, 0);
    if (!password) return 0;
  }
  callback = ABC_BitCoin_Event_Callback; // *(tABC_BitCoin_Event_Callback *)&jcallback;
  bcInfo = *(void **)&bitcoinInfo;    // holds bitcoinInfo
  errorp = *(tABC_Error **)&jerrorp;
  result = (tABC_CC)ABC_DataSyncAccount((char const *)username, (char const *)password,
                                        callback, bcInfo, errorp);
  jresult = (jint)result;
  if (username) (*jenv)->ReleaseStringUTFChars(jenv, jusername, (const char *)username);
  if (password) (*jenv)->ReleaseStringUTFChars(jenv, jpassword, (const char *)password);
  return jresult;
}

Java_com_airbitz_api_CoreAPI_coreDataSyncWallet(JNIEnv *jenv, jclass jcls, jstring jusername, jstring jpassword, jstring juuid, jlong jerrorp) {
  jint jresult = 0 ;
  char *username = (char *) 0 ;
  char *password = (char *) 0 ;
  char *uuid = (char *) 0 ;
  tABC_BitCoin_Event_Callback callback = (tABC_BitCoin_Event_Callback) 0 ;
  void *bcInfo = (void *) 0 ;
  tABC_Error *errorp = (tABC_Error *) 0 ;
  tABC_CC result;

  (void)jenv;
  (void)jcls;

  username = 0;
  if (jusername) {
    username = (char *)(*jenv)->GetStringUTFChars(jenv, jusername, 0);
    if (!username) return 0;
  }
  password = 0;
  if (jpassword) {
    password = (char *)(*jenv)->GetStringUTFChars(jenv, jpassword, 0);
    if (!password) return 0;
  }
  uuid = 0;
  if (juuid) {
    uuid = (char *)(*jenv)->GetStringUTFChars(jenv, juuid, 0);
    if (!uuid) return 0;
  }
  callback = ABC_BitCoin_Event_Callback; // *(tABC_BitCoin_Event_Callback *)&jcallback;
  bcInfo = *(void **)&bitcoinInfo;    // holds bitcoinInfo
  errorp = *(tABC_Error **)&jerrorp;
  result = (tABC_CC)ABC_DataSyncWallet((char const *)username, (char const *)password,
                                       (char const *)uuid, callback, bcInfo, errorp);
  jresult = (jint)result;
  if (username) (*jenv)->ReleaseStringUTFChars(jenv, jusername, (const char *)username);
  if (password) (*jenv)->ReleaseStringUTFChars(jenv, jpassword, (const char *)password);
  if (uuid) (*jenv)->ReleaseStringUTFChars(jenv, juuid, (const char *)uuid);
  return jresult;
}

Java_com_airbitz_api_CoreAPI_coreSweepKey(JNIEnv *jenv, jclass jcls, jstring jusername, jstring jpassword, jstring juuid, jstring jwif, jlong ppchar, jlong jerrorp) {
  jint jresult = 0 ;
  char *username = (char *) 0 ;
  char *password = (char *) 0 ;
  char *uuid = (char *) 0 ;
  char *wif = (char *) 0 ;
  char **ret = (char **) 0 ;
  tABC_Sweep_Done_Callback callback = (tABC_Sweep_Done_Callback) 0 ;
  void *bcInfo = (void *) 0 ;
  tABC_Error *errorp = (tABC_Error *) 0 ;
  tABC_CC result;

  (void)jenv;
  (void)jcls;

  username = 0;
  if (jusername) {
    username = (char *)(*jenv)->GetStringUTFChars(jenv, jusername, 0);
    if (!username) return 0;
  }
  password = 0;
  if (jpassword) {
    password = (char *)(*jenv)->GetStringUTFChars(jenv, jpassword, 0);
    if (!password) return 0;
  }
  uuid = 0;
  if (juuid) {
    uuid = (char *)(*jenv)->GetStringUTFChars(jenv, juuid, 0);
    if (!uuid) return 0;
  }
  wif = 0;
  if (jwif) {
    wif = (char *)(*jenv)->GetStringUTFChars(jenv, jwif, 0);
    if (!wif) return 0;
  }
  ret = *(char ***)&ppchar;
  errorp = *(tABC_Error **)&jerrorp;
  result = (tABC_CC)ABC_SweepKey((char const *)username, (char const *)password,
                    (char const *)uuid, (char const *)wif, ret, callback, bcInfo, errorp);
  jresult = (jint)result;
  if (username) (*jenv)->ReleaseStringUTFChars(jenv, jusername, (const char *)username);
  if (password) (*jenv)->ReleaseStringUTFChars(jenv, jpassword, (const char *)password);
  if (uuid) (*jenv)->ReleaseStringUTFChars(jenv, juuid, (const char *)uuid);
  if (wif) (*jenv)->ReleaseStringUTFChars(jenv, jwif, (const char *)wif);
  return jresult;
}


JNIEXPORT jint JNICALL
Java_com_airbitz_api_CoreAPI_coreWatcherLoop(JNIEnv *jenv, jclass jcls, jstring juuid, jlong jerrorp) {
  jint jresult = 0 ;
  char *uuid = (char *) 0 ;
  tABC_BitCoin_Event_Callback callback = (tABC_BitCoin_Event_Callback) 0 ;
  void *bcInfo = (void *) 0 ;
  tABC_Error *errorp = (tABC_Error *) 0 ;
  tABC_CC result;

  (void)jenv;
  (void)jcls;

  uuid = 0;
  if (juuid) {
    uuid = (char *)(*jenv)->GetStringUTFChars(jenv, juuid, 0);
    if (!uuid) return 0;
  }

  callback = ABC_BitCoin_Event_Callback; // *(tABC_BitCoin_Event_Callback *)&jcallback;
  bcInfo = *(void **)&bitcoinInfo;    // holds bitcoinInfo

  errorp = *(tABC_Error **)&jerrorp;

//  __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "coreWatcherLoop: starting a watcher");
  result = (tABC_CC)ABC_WatcherLoop((char const *)uuid, callback, bcInfo, errorp);
//  __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "coreWatcherLoop: loop returned! Not good");
  jresult = (jint)result;
  if (uuid) (*jenv)->ReleaseStringUTFChars(jenv, juuid, (const char *)uuid);
  return jresult;
}

JNIEXPORT jboolean JNICALL
Java_com_airbitz_api_CoreAPI_RegisterAsyncCallback (JNIEnv * env, jobject obj)
{
		// convert local to global reference
        // (local will die after this method call)
		g_obj = (*env)->NewGlobalRef(env, obj);

		// Save global vm
        int status = (*env)->GetJavaVM(env, &g_vm);
        if(status != 0) {
            __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "RegisterAsyncCallback global vm fail");
            return false;
        }

		// save refs for callback
		jclass g_clazz = (*env)->GetObjectClass(env, g_obj);
		if (g_clazz == NULL) {
            __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "RegisterAsyncCallback failed to find class");
            return false;
		}

		g_mid_callback = (*env)->GetMethodID(env, g_clazz, "callbackAsyncBitcoinInfo", "(J)V");
		if (g_mid_callback == NULL) {
            __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "RegisterAsyncCallback unable to get method ref");
            return false;
		}

		return true;
}


/*
 * Return String from ptr to string
 */
JNIEXPORT jstring JNICALL
Java_com_airbitz_api_CoreAPI_getStringAtPtr( JNIEnv *env, jobject obj, jlong ptr )
{
    char *buf = *(char **) &ptr; //*(unsigned int **)&jarg4;
    jstring jresult = (*env)->NewStringUTF(env, buf);
    return jresult;
}

/*
 * Return byte array from ptr
 */
JNIEXPORT jbyteArray JNICALL
Java_com_airbitz_api_CoreAPI_getBytesAtPtr( JNIEnv *env, jobject obj, jlong ptr , jint len)
{
    jbyteArray result = (*env)->NewByteArray(env, len);
    (*env)->SetByteArrayRegion( env, result, 0, len, *(const jbyte **) &ptr );
    return result;
}

/*
 * Return 64 bit long from ptr
 */
JNIEXPORT jlong JNICALL
Java_com_airbitz_api_CoreAPI_get64BitLongAtPtr(JNIEnv *env, jobject obj, jlong ptr)
{
    char *base = *(char **) &ptr;
    int i=0;
    char value=0;
    long long result = 0;
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util_Get64BitLongAtPtr", "ptr=%p", (void *) base);
    for(i=0; i<8; i++) {
        long long value = base[i];
//        __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "value=%llx", value);
        result |= ( value << (i*8) );
    }
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "result=%llx", result);
    return (jlong) result;
}

/*
 * Set 64 bit long at ptr
 */
JNIEXPORT void JNICALL
Java_com_airbitz_api_CoreAPI_set64BitLongAtPtr(JNIEnv *jenv, jclass jcls, jlong obj, jlong value) {
    unsigned char *base = *(unsigned char **) &obj;
    int i=0;
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util_Set64BitLongAtPtr", "value=%llx", value);
    for(i=0; i<8; i++) {
        base[i] = (unsigned char) ((value >> (i*8)) & 0xff);
//      __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "base[i]=%x", base[i]);
    }
}

/*
 * Return 64 bit long from pointer
 */
JNIEXPORT jlong JNICALL
Java_com_airbitz_api_CoreAPI_getLongAtPtr(jlong *obj)
{
    return *obj;
}

/*
 * Proper conversion without SWIG problems
*/
JNIEXPORT jlong JNICALL
Java_com_airbitz_api_CoreAPI_ParseAmount(JNIEnv *jenv, jclass jcls, jstring jarg1, jint decimalplaces) {
  tABC_CC result;

  (void)jenv;
  (void)jcls;

      char *instring = (char *) 0 ;

      instring = 0;
      if (jarg1) {
        instring = (char *)(*jenv)->GetStringUTFChars(jenv, jarg1, 0);
        if (!instring) return 0;
      }

  int64_t arg2 = 0; //*(int64_t **)&outp;

  unsigned int arg3 = (unsigned int)decimalplaces;

//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util_ParseAmount", "string=%c", arg1);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util_ParseAmount", "ppchar=%p", (void *) arg2);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util_ParseAmount", "decimals=%d", arg3);
  result = (tABC_CC)ABC_ParseAmount(instring,&arg2,arg3);
  return arg2;
}

/*
 * Proper conversion without SWIG problems
*/
JNIEXPORT jint JNICALL
Java_com_airbitz_api_CoreAPI_FormatAmount(JNIEnv *jenv, jclass jcls, jlong satoshi, jlong ppchar, jlong decimalplaces, jboolean addSign, jlong perror) {
  jint jresult = 0 ;
  int64_t arg1 = satoshi;
  char **arg2 = (char **) 0 ;
  unsigned int arg3 ;
  unsigned int arg4 ;
  tABC_Error *argError = (tABC_Error *) 0 ;
  tABC_CC result;

  (void)jenv;
  (void)jcls;

  arg2 = *(char ***)&ppchar;
  arg3 = (unsigned int)decimalplaces;
  arg4 = addSign==false? 0 : 1;
  argError = *(tABC_Error **)&perror;

//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util_FormatAmount", "satoshi=%llx", arg1);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util_FormatAmount", "ppchar=%p", (void *) arg2);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util_FormatAmount", "decimals=%d", arg3);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util_FormatAmount", "perror=%p", (void *) argError);
  result = (tABC_CC)ABC_FormatAmount(arg1,arg2,arg3,arg4,argError);
  jresult = (jint)result;
  return jresult;
}

/*
 * Proper conversion to currency without SWIG problems
*/
JNIEXPORT jint JNICALL
Java_com_airbitz_api_CoreAPI_satoshiToCurrency( JNIEnv *jenv, jobject obj,
    jstring jarg1, jstring jarg2, jlong satoshi, jlong currencyp, jint currencyNumber, jlong error )
{
    tABC_CC result;
    double *arg4;
    arg4 = *(double **)&currencyp;
    int number = (int) currencyNumber;
    int64_t sat = satoshi;
    tABC_Error *argError = (tABC_Error *) 0 ;
    argError = *(tABC_Error **)&error;

      char *username = (char *) 0 ;
      char *password = (char *) 0 ;

      username = 0;
      if (jarg1) {
        username = (char *)(*jenv)->GetStringUTFChars(jenv, jarg1, 0);
        if (!username) return 0;
      }
      password = 0;
      if (jarg2) {
        password = (char *)(*jenv)->GetStringUTFChars(jenv, jarg2, 0);
        if (!password) return 0;
      }
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "username=%s", username);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "password=%s", password);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "unsigned satoshi=%llu", sat);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "hex satoshi=%llx", sat);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "signed satoshi=%lld", sat);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "currencyNumber=%d", currencyNumber);

    result = ABC_SatoshiToCurrency(username, password, sat, arg4, currencyNumber, argError);

//    if(result==0) {
//        __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "hex currency=%llx", *currencyp);
//        __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "float currency=%f", *currencyp);
//    }
    return result;
}

