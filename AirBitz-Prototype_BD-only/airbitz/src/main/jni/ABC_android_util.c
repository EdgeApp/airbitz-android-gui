#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include <ABC_android.h>
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

    (*g_env)->CallVoidMethod(g_env, g_obj, g_mid_callback, (void *) pInfo); //walletUUID, txId);

//
//	if ((*g_env)->ExceptionCheck(g_env)) {
//		(*g_env)->ExceptionDescribe(g_env);
//	}

	(*g_vm)->DetachCurrentThread(g_vm);
}

void ABC_BitCoin_Event_Callback(const tABC_AsyncBitCoinInfo *pInfo)
{
    bitcoinCallback(pInfo);
//    if (pInfo->eventType == ABC_AsyncEventType_IncomingBitCoin)
//    {
////        NSString *walletUUID = [NSString stringWithUTF8String:pInfo->szWalletUUID];
////        NSString *txId = [NSString stringWithUTF8String:pInfo->szTxID];
////        NSArray *params = [NSArray arrayWithObjects: walletUUID, txId, nil];
////        [mainId performSelectorOnMainThread:@selector(launchReceiving:) withObject:params waitUntilDone:NO];
//        __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "incoming bitcoin received");
//    } else if (pInfo->eventType == ABC_AsyncEventType_BlockHeightChange) {
//        __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "Block Height change received");
////        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIFICATION_BLOCK_HEIGHT_CHANGE object:mainId];
//    } else if (pInfo->eventType == ABC_AsyncEventType_ExchangeRateUpdate) {
//        __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "Exchange Rate Update received");
////        NSLog(@"Exchange rate change fired!!!!!!!!!!!");
////        [[NSNotificationCenter defaultCenter] postNotificationName:NOTIFICATION_EXCHANGE_RATE_CHANGE object:mainId];
//    }
}

// Custom initialization to handle callbacks
JNIEXPORT jint JNICALL
Java_com_airbitz_api_CoreAPI_coreInitialize(JNIEnv *jenv, jclass jcls, jstring jrootDir, jstring jcertDir, jstring jseed, jlong jseedLength, jlong jerrorp) {
  jint jresult = 0 ;
  char *root = (char *) 0 ;
  char *cert = (char *) 0 ;
  tABC_BitCoin_Event_Callback callback = (tABC_BitCoin_Event_Callback) 0 ;
  void *bcInfo = (void *) 0 ;
  unsigned char *seed = (unsigned char *) 0 ;
  unsigned int seedLength ;
  tABC_Error *errorp = (tABC_Error *) 0 ;
  tABC_CC result;

  (void)jenv;
  (void)jcls;

  root = 0;
  if (jrootDir) {
    root = (char *)(*jenv)->GetStringUTFChars(jenv, jrootDir, 0);
    if (!root) return 0;
  }
  cert = 0;
  if (jcertDir) {
    cert = (char *)(*jenv)->GetStringUTFChars(jenv, jcertDir, 0);
    if (!cert) return 0;
  }
  callback = ABC_BitCoin_Event_Callback; // *(tABC_BitCoin_Event_Callback *)&jcallback;
  bcInfo = *(void **)&bitcoinInfo;    // holds bitcoinInfo
  seed = 0;
  if (jseed) {
    seed = (unsigned char *)(*jenv)->GetStringUTFChars(jenv, jseed, 0);
    if (!seed) return 0;
  }
  seedLength = (unsigned int)jseedLength;
  errorp = *(tABC_Error **)&jerrorp;
  result = (tABC_CC)ABC_Initialize((char const *)root, (char const *)cert, callback, bcInfo, (unsigned char const *)seed, seedLength, errorp);
  jresult = (jint)result;
  if (root) (*jenv)->ReleaseStringUTFChars(jenv, jrootDir, (const char *)root);
  if (seed) (*jenv)->ReleaseStringUTFChars(jenv, jseed, (const char *)seed);
  return jresult;
}

JNIEXPORT jboolean JNICALL
Java_com_airbitz_api_CoreAPI_RegisterAsyncCallback (JNIEnv * env, jobject obj)
{
        bool returnValue = true;
		// convert local to global reference
        // (local will die after this method call)
		g_obj = (*env)->NewGlobalRef(env, obj);

		// Save global vm
        int status = (*env)->GetJavaVM(env, &g_vm);
        if(status != 0) {
            __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "RegisterAsyncCallback global vm fail");
        }

		// save refs for callback
		jclass g_clazz = (*env)->GetObjectClass(env, g_obj);
		if (g_clazz == NULL) {
            __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "RegisterAsyncCallback failed to find class");
		}

		g_mid_callback = (*env)->GetMethodID(env, g_clazz, "callbackAsyncBitcoinInfo", "(J)V");
		if (g_mid_callback == NULL) {
            __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "RegisterAsyncCallback unable to get method ref");
		}

		return (jboolean)returnValue;
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
Java_com_airbitz_api_CoreAPI_FormatAmount(JNIEnv *jenv, jclass jcls, jlong satoshi, jlong ppchar, jlong decimalplaces, jlong perror) {
  jint jresult = 0 ;
  int64_t arg1 = satoshi;
  char **arg2 = (char **) 0 ;
  unsigned int arg3 ;
  tABC_Error *argError = (tABC_Error *) 0 ;
  tABC_CC result;

  (void)jenv;
  (void)jcls;

  arg2 = *(char ***)&ppchar;
  arg3 = (unsigned int)decimalplaces;
  argError = *(tABC_Error **)&perror;

//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util_FormatAmount", "satoshi=%llx", arg1);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util_FormatAmount", "ppchar=%p", (void *) arg2);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util_FormatAmount", "decimals=%d", arg3);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util_FormatAmount", "perror=%p", (void *) argError);
  result = (tABC_CC)ABC_FormatAmount(arg1,arg2,arg3,argError);
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

/*
 * Set Wallet order
 */
JNIEXPORT jint JNICALL
Java_com_airbitz_api_CoreAPI_setWalletOrder( JNIEnv *jenv, jobject obj, jstring jarg1, jstring jarg2, jobjectArray stringArray, jlong jarg5 )
{
      jint jresult = 0 ;
      int i;
      char *arg1 = (char *) 0 ;
      char *arg2 = (char *) 0 ;
      char **arg3 = (char **) 0;
      tABC_Error *arg5 = (tABC_Error *) 0 ;
      tABC_CC result;

      arg1 = 0;
      if (jarg1) {
        arg1 = (char *)(*jenv)->GetStringUTFChars(jenv, jarg1, 0);
        if (!arg1) return 0;
      }
      arg2 = 0;
      if (jarg2) {
        arg2 = (char *)(*jenv)->GetStringUTFChars(jenv, jarg2, 0);
        if (!arg2) return 0;
      }

    unsigned int count = (unsigned int) (*jenv)->GetArrayLength(jenv, stringArray);
    const char **param = (const char **) malloc(count*sizeof(const char *));
    for(i = 0; i < count; i++)
    {
        jstring string = (jstring) (*jenv)->GetObjectArrayElement(jenv, stringArray, i);
        param[i] = (*jenv)->GetStringUTFChars(jenv, string, 0);
//        __android_log_write(ANDROID_LOG_INFO, "ABC_android_util", param[i]);
    }


    arg3 = (char **) param;
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "count=%d", count);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "username=%s", arg1);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "password=%s", arg2);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "arg3=%p", arg3);
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "param=%p", param);
    arg5 = *(tABC_Error **)&jarg5;
//    __android_log_print(ANDROID_LOG_INFO, "ABC_android_util", "arg5=%p", arg5);

    result = (tABC_CC) ABC_SetWalletOrder((char const *)arg1,(char const *)arg2,arg3,count,arg5);

    for (i=0; i<count; i++) {
        jstring string = (jstring) (*jenv)->GetObjectArrayElement(jenv, stringArray, i);
        (*jenv)->ReleaseStringUTFChars(jenv, string, param[i]);
    }
    jresult = (jint)result;

    return jresult;
}

